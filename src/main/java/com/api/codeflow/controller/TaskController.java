package com.api.codeflow.controller;

import com.api.codeflow.dto.request.SubmitCodeDto;
import com.api.codeflow.dto.response.SuccessSolution;
import com.api.codeflow.exception.*;
import com.api.codeflow.service.CompilerService;
import com.api.codeflow.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskService taskService;
    private final CompilerService compilerService;

    @GetMapping("/{id}")
    public ResponseEntity<?> findTaskInfoById(@PathVariable Long id, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(taskService.findTaskById(id, request));
        } catch (NotFoundException e) {
            return new ResponseEntity<>("Task not founded!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while finding task by Id: " + e.getMessage());
            return new ResponseEntity<>("Server Error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<?> findAllTasks(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(taskService.findAllTasks(request));
        } catch (Exception e) {
            log.error("Error while finding all Tasks: " + e.getMessage());
            return new ResponseEntity<>("Server Error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{taskId}/submit")
    public ResponseEntity<?> submitSolution(@PathVariable Long taskId,
                                            @RequestBody SubmitCodeDto dto) {
        long start = System.currentTimeMillis();
        try {
            SuccessSolution success = compilerService.checkSolution(taskId, dto);
            long end = System.currentTimeMillis(); // завершение

            double durationSeconds = (end - start) / 1000.0;
            log.info("✅ Success Solution:\n{}", decodeNewlines(success.toString()));
            log.info("⏳ Total execution time: {} seconds", durationSeconds); // ⏱️ лог времени

            return ResponseEntity.ok(createResponse("success", success));

/* Old version: перед замерами
            SuccessSolution success = compilerService.checkSolution(taskId, dto);
            log.info("✅ Success Solution:\n{}", decodeNewlines(success.toString()));
            return ResponseEntity.ok(createResponse("success", success));
 */
        } catch (WrongSolutionException e) {
            log.info("❌ Wrong Solution:\n{}", decodeNewlines(e.getPayload().toString()));
            return ResponseEntity.ok(createResponse("wrong", e.getPayload()));
        } catch (CompilationErrorException e) {
            log.info("💥 Compilation Error:\n{}", decodeNewlines(e.getPayload().toString()));
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("compile_error", e.getPayload()));
        } catch (OutOfMemoryException e) {
            log.info("🚫 Out of Memory:\n{}", decodeNewlines(e.getPayload().toString()));
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("memory_error", e.getPayload()));
        } catch (TimeLimitException e) {
            log.info("⏱️ Time Limit Exceeded:\n{}", decodeNewlines(e.getPayload().toString()));
            return ResponseEntity.ok(createResponse("time_limit", e.getPayload()));
        } catch (Exception e) {
            log.error("🔥 Server Error: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse("error", "Internal server error: " + e.getMessage()));
        }
    }

    private Map<String, Object> createResponse(String status, Object payload) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("result", payload);
        return map;
    }

    private String decodeNewlines(String s) {
        return s.replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}