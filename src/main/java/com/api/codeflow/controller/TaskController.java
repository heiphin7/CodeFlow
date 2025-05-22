package com.api.codeflow.controller;

import com.api.codeflow.dto.request.SubmitCodeDto;
import com.api.codeflow.exception.NotFoundException;
import com.api.codeflow.service.CompilerService;
import com.api.codeflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskService taskService;
    private final CompilerService compilerService;

    @GetMapping("/{id}")
    public ResponseEntity<?> findTaskInfoById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.findTaskById(id));
        } catch (NotFoundException e) {
            return new ResponseEntity<>("Task not founded!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while finding task by Id: " + e.getMessage());
            return new ResponseEntity<>("Server Error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<?> findAllTasks() {
        try {
            return ResponseEntity.ok(taskService.findAllTasks());
        } catch (Exception e) {
            log.error("Error while finding all Tasks: " + e.getMessage());
            return new ResponseEntity<>("Server Error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{taskId}/submit")
    public ResponseEntity<?> submitSolution(@PathVariable Long taskId,
                                            @RequestBody SubmitCodeDto dto) {
        try {
            compilerService.checkSolution(taskId, dto);
        }
    }
}
