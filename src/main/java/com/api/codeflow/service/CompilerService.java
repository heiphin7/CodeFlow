package com.api.codeflow.service;

import com.api.codeflow.dto.judge0.BatchSubmissionResult;
import com.api.codeflow.dto.judge0.SubmissionRequest;
import com.api.codeflow.dto.request.SubmitCodeDto;
import com.api.codeflow.dto.response.SuccessSolution;
import com.api.codeflow.dto.response.WrongSolution;
import com.api.codeflow.exception.WrongSolutionException;
import com.api.codeflow.model.Task;
import com.api.codeflow.model.TestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilerService {
    private final TaskService taskService;
    private final Judge0Client judge0Client;

    public SuccessSolution checkSolution(Long taskId, SubmitCodeDto dto) throws InterruptedException {
        Task task = taskService.findByIdWithTestCases(taskId);

        log.info("Number of test cases: {}", task.getTestCases().size());

        // Сортируем тесты по номеру
        List<TestCase> sortedCases = task.getTestCases().stream()
                .sorted(Comparator.comparing(TestCase::getTestNumber))
                .toList();

        // Готовим batch-запрос
        List<SubmissionRequest> submissions = sortedCases.stream()
                .map(tc -> {
                    SubmissionRequest req = new SubmissionRequest();
                    req.setLanguage_id(mapLanguage(dto.getLanguage()));
                    req.setSource_code(dto.getSolution());
                    req.setStdin(tc.getInput());
                    req.setExpected_output(tc.getExceptedOutput());
                    req.setCpu_time_limit(task.getTimeLimit());
                    req.setMemory_limit(task.getMemoryLimit());
                    return req;
                })
                .toList();

        // Отправляем все тесты разом и ждём результатов
        List<String> tokens  = judge0Client.submitBatch(submissions);
        List<BatchSubmissionResult> results = judge0Client.getBatchResults(tokens);

        double maxMemory = 0;
        int maxTime    = 0;

        // Проходим по результатам в порядке тестов
        for (int i = 0; i < results.size(); i++) {
            BatchSubmissionResult res = results.get(i);
            TestCase tc = sortedCases.get(i);
            int statusId = res.getStatus().getId();

            if (statusId == 3) {
                // Accepted — учитываем ресурсные метрики
                double memMB = res.getMemory() / 1024.0;
                maxMemory = Math.max(maxMemory, memMB);
                int    tms   = (int)(Double.parseDouble(res.getTime()) * 1000);
                maxTime   = Math.max(maxTime, tms);
            }
            else if (statusId == 4) {
                // Wrong Answer — stdout содержит вывод пользователя
                WrongSolution wrong = new WrongSolution();
                wrong.setTestCaseNumber(tc.getTestNumber());
                wrong.setInput(tc.getInput());
                wrong.setExceptedOutput(tc.getExceptedOutput());
                wrong.setProgramOutput(res.getStdout());
                throw new WrongSolutionException(wrong);
            }
            else if (statusId == 6) {
                // Compilation Error
                WrongSolution wrong = new WrongSolution();
                wrong.setTestCaseNumber(tc.getTestNumber());
                wrong.setInput(tc.getInput());
                wrong.setExceptedOutput(tc.getExceptedOutput());
                wrong.setProgramOutput(res.getCompile_output());
                throw new WrongSolutionException(wrong);
            }
            else {
                // Другие статусы (TLE, RTE и т.п.) — берём stderr или message
                WrongSolution wrong = new WrongSolution();
                wrong.setTestCaseNumber(tc.getTestNumber());
                wrong.setInput(tc.getInput());
                wrong.setExceptedOutput(tc.getExceptedOutput());
                String output = res.getStderr() != null
                        ? res.getStderr()
                        : res.getMessage();
                wrong.setProgramOutput(output);
                throw new WrongSolutionException(wrong);
            }
        }

        // Все тесты прошли
        SuccessSolution ok = new SuccessSolution();
        ok.setMemoryUsage(maxMemory);
        ok.setTimeUsage(maxTime);
        return ok;
    }

    private Integer mapLanguage(String lang) {
        return switch (lang.toLowerCase()) {
            case "java"   -> 62;
            case "python" -> 71;
            default       -> throw new IllegalArgumentException("Unsupported language: " + lang);
        };
    }
}

