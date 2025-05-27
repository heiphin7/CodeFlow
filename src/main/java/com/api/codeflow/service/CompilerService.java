package com.api.codeflow.service;

import com.api.codeflow.dto.judge0.BatchSubmissionResult;
import com.api.codeflow.dto.judge0.SubmissionRequest;
import com.api.codeflow.dto.request.SubmitCodeDto;
import com.api.codeflow.dto.response.SuccessSolution;
import com.api.codeflow.dto.response.WrongSolution;
import com.api.codeflow.exception.TimeLimitExceededException;
import com.api.codeflow.exception.WrongSolutionException;
import com.api.codeflow.model.Submission;
import com.api.codeflow.model.Task;
import com.api.codeflow.model.TestCase;
import com.api.codeflow.model.User;
import com.api.codeflow.repository.SubmissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilerService {
    private final TaskService taskService;
    private final Judge0Client judge0Client;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

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
                    req.setSource_code(dto.getSolution());
                    req.setStdin(tc.getInput());
                    req.setExpected_output(tc.getExceptedOutput());
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

            // ❗ Запись результата отправки
            submission.setStatus("Time Limit Exceeded");
            submission.setMemoryUsage(0.0);
            submission.setExecutionTime((double) task.getTimeLimit());
            submissionRepository.save(submission);

            user.getSubmissions().add(submission);
            userService.updateUser(user);

            WrongSolution wrong = new WrongSolution();
            wrong.setTestCaseNumber(tc.getTestNumber());
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
                String output;
                String readableStatus;

                switch (status) {
                    case 4:  // Wrong Answer
                        output = r.getStdout();
                        readableStatus = "Wrong Answer";
                        break;
                    case 5:
                        output = "Time Limit Exceeded";
                        readableStatus = "Time Limit Exceeded";
                        break;
                    case 6:
                        output = r.getCompile_output();
                        readableStatus = "Compilation Error";
                        break;
                    default:
                        output = r.getStderr() != null ? r.getStderr() : r.getMessage();
                        readableStatus = "Runtime Error";
                }

                // ❗ Запись неуспешной отправки
                submission.setStatus(readableStatus);
                submission.setMemoryUsage(r.getMemory() / 1024.0);
                submission.setExecutionTime(Double.parseDouble(r.getTime()));
                submissionRepository.save(submission);

                user.getSubmissions().add(submission);
                userService.updateUser(user);

                WrongSolution wrong = new WrongSolution();
                wrong.setTestCaseNumber(tc.getTestNumber());
                wrong.setInput(tc.getInput());
                wrong.setExceptedOutput(tc.getExceptedOutput());
                wrong.setProgramOutput(output);

                throw new WrongSolutionException(wrong);
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

        user.getSubmissions().add(submission);
        userService.updateUser(user);

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

