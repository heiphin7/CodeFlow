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

        for (int i = 0; i < results.size(); i++) {
            BatchSubmissionResult r = results.get(i);
            TestCase tc = sortedCases.get(i);
            int status = r.getStatus().getId();

            if (status != 3) {
                WrongSolution wrong = new WrongSolution();
                wrong.setTestCaseNumber(tc.getTestNumber());
                wrong.setInput(tc.getInput());
                wrong.setExceptedOutput(tc.getExceptedOutput());

                String output;
                switch (status) {
                    case 4:  // Wrong Answer
                        output = r.getStdout();
                        break;
                    case 5:  // Time Limit Exceeded
                        output = "Time Limit Exceeded";
                        break;
                    case 6:  // Compilation Error
                        output = r.getCompile_output();
                        break;
                    default: // Runtime Error и прочие
                        output = r.getStderr() != null ? r.getStderr() : r.getMessage();
                }
                wrong.setProgramOutput(output);
                throw new WrongSolutionException(wrong);
            }
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
            case "java"        -> 62;  // Java (OpenJDK 17)
            case "python"      -> 71;  // Python (3.8.1)
            case "python3"     -> 71;
            case "cpp", "c++"  -> 54;  // C++ (GCC 9.2.0)
            case "c"           -> 50;  // C (GCC 9.2.0)
            case "javascript"  -> 63;  // JavaScript (Node.js 12.14.0)
            case "typescript"  -> 74;  // TypeScript (3.7.4)
            case "csharp", "cs"-> 51;  // C# (Mono 6.6.0.161)
            case "ruby"        -> 72;  // Ruby (2.7.0)
            case "go", "golang"-> 60;  // Go (1.13.5)
            case "swift"       -> 83;  // Swift (5.2.3)
            case "kotlin"      -> 78;  // Kotlin (1.3.70)
            case "rust"        -> 73;  // Rust (1.40.0)
            default            -> throw new IllegalArgumentException("Unsupported language: " + lang);
        };
    }

}

