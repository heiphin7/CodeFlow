package com.api.codeflow.service;

import com.api.codeflow.dto.judge0.BatchSubmissionResult;
import com.api.codeflow.dto.judge0.SubmissionRequest;
import com.api.codeflow.dto.request.SubmitCodeDto;
import com.api.codeflow.dto.response.SuccessSolution;
import com.api.codeflow.dto.response.WrongSolution;
import com.api.codeflow.exception.TimeLimitExceededException;
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
                    req.setCpu_time_limit(task.getTimeLimit());
                    req.setWall_time_limit(task.getTimeLimit());
                    return req;
                })
                .toList();

        // Отправляем все тесты разом и ждём результатов
        List<String> tokens  = judge0Client.submitBatch(submissions);
        List<BatchSubmissionResult> results;
        try {
            // здесь может прилететь TimeLimitExceededException
            results = judge0Client.getBatchResults(tokens);
        } catch (TimeLimitExceededException e) {
            // мапим на WrongSolution
            int tcNum = e.getTestCaseNumber();
            TestCase tc = sortedCases.stream()
                    .filter(t -> t.getTestNumber() == tcNum)
                    .findFirst()
                    .orElse(sortedCases.get(tcNum-1));

            WrongSolution wrong = new WrongSolution();
            wrong.setTestCaseNumber(tcNum);
            wrong.setInput(tc.getInput());
            wrong.setExceptedOutput(tc.getExceptedOutput());
            wrong.setProgramOutput("Time Limit Exceeded");
            throw new WrongSolutionException(wrong);
        }

        // все здесь гарантированно Accepted, собираем метрики:
        double maxMemory = results.stream()
                .mapToDouble(r -> r.getMemory()/1024.0)
                .max().orElse(0);
        int maxTime = results.stream()
                .mapToInt(r -> (int)(Double.parseDouble(r.getTime())*1000))
                .max().orElse(0);


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

