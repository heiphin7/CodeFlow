package com.api.codeflow.service;

import com.api.codeflow.dto.judge0.BatchSubmissionResult;
import com.api.codeflow.dto.judge0.CompilationErrorSolution;
import com.api.codeflow.dto.judge0.SubmissionRequest;
import com.api.codeflow.dto.judge0.TimeLimitSolution;
import com.api.codeflow.dto.request.SubmitCodeDto;
import com.api.codeflow.dto.response.SuccessSolution;
import com.api.codeflow.dto.response.WrongSolution;
import com.api.codeflow.exception.*;
import com.api.codeflow.model.*;
import com.api.codeflow.repository.SubmissionRepository;
import com.api.codeflow.repository.TaskSolutionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilerService {
    private final TaskService taskService;
    private final Judge0Client judge0Client;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final TaskSolutionRepository taskSolutionRepository;
    private final AiService aiService;
    private final TaskSolutionService taskSolutionService;

    public SuccessSolution checkSolution(Long taskId, SubmitCodeDto dto) throws InterruptedException {
        Task task = taskService.findByIdWithTestCases(taskId);
        User user = userService.findById(dto.getUserId());

        // Формируем Submission
        Submission submission = new Submission();
        submission.setTask(task);
        submission.setUser(user);
        submission.setCode(dto.getSolution());
        submission.setLanguage(dto.getLanguage());
        submission.setCreatedAt(new Date());

        List<TestCase> sortedCases = task.getTestCases().stream()
                .sorted(Comparator.comparing(TestCase::getTestNumber))
                .toList();

        List<SubmissionRequest> submissions = sortedCases.stream()
                .map(tc -> {
                    SubmissionRequest req = new SubmissionRequest();
                    req.setLanguage_id(mapLanguage(dto.getLanguage()));
                    req.setSource_code(encodeBase64(dto.getSolution()));
                    req.setStdin(encodeBase64(tc.getInput()));
                    req.setExpected_output(encodeBase64(tc.getExceptedOutput()));
                    req.setCpu_time_limit(task.getTimeLimit());
                    req.setWall_time_limit(task.getTimeLimit());
                    req.setMemory_limit(task.getMemoryLimit());
                    return req;
                })
                .toList();

        List<String> tokens = judge0Client.submitBatch(submissions);
        List<BatchSubmissionResult> results;

        try {
            results = judge0Client.getBatchResults(tokens);
        } catch (TimeLimitExceededException e) {
            TestCase tc = sortedCases.stream()
                    .filter(t -> t.getTestNumber() == e.getTestCaseNumber())
                    .findFirst()
                    .orElse(sortedCases.get(e.getTestCaseNumber() - 1));

            submission.setStatus("Time Limit Exceeded");
            submission.setMemoryUsage(Double.valueOf(task.getMemoryLimit()));
            submission.setExecutionTime((double) task.getTimeLimit());
            submissionRepository.save(submission);
            userService.updateUser(user);

            TimeLimitSolution tle = new TimeLimitSolution();
            tle.setTestCaseNumber(tc.getTestNumber());
            tle.setInput(tc.getInput());
            tle.setExceptedOutput(tc.getExceptedOutput());
            tle.setProgramOutput("Time Limit Exceeded");

            throw new TimeLimitException(tle);
        }

        for (int i = 0; i < results.size(); i++) {
            BatchSubmissionResult r = results.get(i);
            TestCase tc = sortedCases.get(i);
            int status = r.getStatus().getId();

            if (status != 3) {
                String output;
                String readableStatus;

                switch (status) {
                    case 4: { // Wrong Answer
                        output = r.getStdout();
                        readableStatus = "Wrong Answer";

                        submission.setStatus(readableStatus);
                        submission.setErrorType("WA");
                        submission.setSuccess(false);
                        submission.setFinal(true);
                        submission.setTestCaseNumber(tc.getTestNumber());
                        submission.setResultOutput(decodeBase64(r.getStdout()));
                        submission.setCompileOutput(decodeBase64(r.getCompile_output()));
                        submission.setStderr(decodeBase64(r.getStderr()));
                        submission.setJudgeRawStatus(r.getStatus().getDescription());
                        submission.setMemoryUsage(r.getMemory() != null ? r.getMemory() / 1024.0 : 0.0);
                        submission.setExecutionTime(parseSafeTime(r.getTime()));
                        submissionRepository.save(submission);
                        userService.updateUser(user);

                        WrongSolution wrong = new WrongSolution();
                        wrong.setTestCaseNumber(tc.getTestNumber());
                        wrong.setInput(tc.getInput());
                        wrong.setExceptedOutput(tc.getExceptedOutput());
                        wrong.setProgramOutput(decodeBase64(output));
                        throw new WrongSolutionException(wrong);
                    }
                    case 5: { // Time Limit Exceeded
                        submission.setStatus("Time Limit Exceeded");
                        submission.setErrorType("TLE");
                        submission.setSuccess(false);
                        submission.setFinal(true);
                        submission.setTestCaseNumber(tc.getTestNumber());
                        submission.setJudgeRawStatus(r.getStatus().getDescription());
                        submission.setMemoryUsage(0.0);
                        submission.setExecutionTime(task.getTimeLimit());
                        submission.setResultOutput(null);
                        submission.setCompileOutput(decodeBase64(r.getCompile_output()));
                        submission.setStderr(decodeBase64(r.getStderr()));
                        submissionRepository.save(submission);
                        userService.updateUser(user);

                        TimeLimitSolution tle = new TimeLimitSolution();
                        tle.setTestCaseNumber(tc.getTestNumber());
                        tle.setInput(tc.getInput());
                        tle.setExceptedOutput(tc.getExceptedOutput());
                        tle.setProgramOutput("Time Limit Exceeded");

                        throw new TimeLimitException(tle);
                    }
                    case 6: { // Compilation Error
                        submission.setStatus("Compilation Error");
                        submission.setErrorType("CE");
                        submission.setSuccess(false);
                        submission.setFinal(true);
                        submission.setTestCaseNumber(tc.getTestNumber());
                        submission.setJudgeRawStatus(r.getStatus().getDescription());
                        submission.setMemoryUsage(r.getMemory() != null ? r.getMemory() / 1024.0 : 0.0);
                        submission.setExecutionTime(parseSafeTime(r.getTime()));
                        submission.setCompileOutput(decodeBase64(r.getCompile_output()));
                        submission.setResultOutput(null);
                        submission.setStderr(decodeBase64(r.getStderr()));
                        submissionRepository.save(submission);
                        userService.updateUser(user);

                        CompilationErrorSolution ce = new CompilationErrorSolution();
                        String decodedCompile = decodeBase64(r.getCompile_output());
                        log.info("Decoded Compilation Output: " + decodedCompile);
                        ce.setTestCaseNumber(tc.getTestNumber());
                        ce.setCompileOutput(decodedCompile);
                        throw new CompilationErrorException(ce);

                    }
                    default: { // Runtime Error / OOM
                        output = r.getStderr() != null ? r.getStderr() : r.getMessage();
                        readableStatus = "Runtime Error";

                        submission.setStatus(readableStatus);
                        submission.setErrorType("RE");
                        submission.setSuccess(false);
                        submission.setFinal(true);
                        submission.setTestCaseNumber(tc.getTestNumber());
                        submission.setJudgeRawStatus(r.getStatus().getDescription());
                        submission.setMemoryUsage(r.getMemory() != null ? r.getMemory() / 1024.0 : 0.0);
                        submission.setExecutionTime(parseSafeTime(r.getTime()));
                        submission.setResultOutput(decodeBase64(r.getStdout()));
                        submission.setCompileOutput(decodeBase64(r.getCompile_output()));
                        submission.setStderr(decodeBase64(r.getStderr()));

                        if (output != null && (
                                output.contains("OutOfMemoryError") ||
                                        output.toLowerCase().contains("memory limit exceeded") ||
                                        output.toLowerCase().contains("killed") ||
                                        output.contains("signal: 9")
                        )) {
                            submission.setStatus("Out Of Memory");
                            submission.setErrorType("OOM");
                            submission.setMemoryUsage(Double.valueOf(task.getMemoryLimit()));
                            submission.setExecutionTime((double) task.getTimeLimit());
                            submissionRepository.save(submission);
                            userService.updateUser(user);

                            WrongSolution oom = new WrongSolution();
                            oom.setTestCaseNumber(tc.getTestNumber());
                            oom.setInput(tc.getInput());
                            oom.setExceptedOutput(tc.getExceptedOutput());
                            oom.setProgramOutput("OutOfMemoryError");
                            throw new OutOfMemoryException(oom);
                        }

                        submissionRepository.save(submission);
                        userService.updateUser(user);

                        WrongSolution runtime = new WrongSolution();
                        runtime.setTestCaseNumber(tc.getTestNumber());
                        runtime.setInput(tc.getInput());
                        runtime.setExceptedOutput(tc.getExceptedOutput());
                        runtime.setProgramOutput(decodeBase64(output));
                        throw new WrongSolutionException(runtime);
                    }
                }
            }
        }

        // Accepted: собираем метрики
        double maxMemory = results.stream()
                .mapToDouble(r -> r.getMemory() / 1024.0)
                .max().orElse(0);
        int maxTime = results.stream()
                .mapToInt(r -> (int)(Double.parseDouble(r.getTime()) * 1000))
                .max().orElse(0);

        submission.setStatus("Accepted");
        submission.setMemoryUsage(maxMemory);
        submission.setExecutionTime(maxTime / 1000.0);
        submissionRepository.save(submission);
        submission.setErrorType("SUCCESS");
        submission.setSuccess(true);
        submission.setFinal(true);
        submission.setJudgeRawStatus("Accepted");


        userService.updateUser(user);

        taskSolutionService.saveOrUpdateIfBetter(
                user,
                task,
                dto.getLanguage(),
                maxTime / 1000.0,
                maxMemory,
                dto.getSolution()
        );

        SuccessSolution ok = new SuccessSolution();
        ok.setMemoryUsage(maxMemory);
        ok.setTimeUsage(maxTime);
        ok.setDto(aiService.getAiAnalysis(dto.getSolution(), task.getDescription(), maxMemory, (double) maxTime));
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
    private String getSourceFileName(String language) {
        return switch (language.toLowerCase()) {
            case "java"      -> "Main.java";
            case "csharp", "cs" -> "Main.cs";
            case "cpp", "c++"   -> "main.cpp";
            case "c"            -> "main.c";
            case "python", "python3" -> "main.py";
            case "javascript"   -> "main.js";
            case "typescript"   -> "main.ts";
            case "kotlin"       -> "Main.kt";
            case "swift"        -> "main.swift";
            case "go", "golang" -> "main.go";
            case "rust"         -> "main.rs";
            case "ruby"         -> "main.rb";
            default             -> null;
        };
    }

    private double parseSafeTime(String time) {
        try {
            return (time != null && !time.isBlank()) ? Double.parseDouble(time) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String encodeBase64(String str) {
        return str != null ? Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8)) : null;
    }

    private String decodeBase64(String value) {
        if (value == null) return null;

        // Удалим переносы строк, чтобы корректно считать padding
        String normalized = value.replaceAll("[\\r\\n]", "");

        try {
            byte[] decoded = Base64.getDecoder().decode(normalized);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value; // если всё-таки невалидная строка — вернём как есть
        }
    }
}