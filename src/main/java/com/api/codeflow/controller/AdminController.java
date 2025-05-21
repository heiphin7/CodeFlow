package com.api.codeflow.controller;

import com.api.codeflow.dto.CreateNewTaskDto;
import com.api.codeflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final TaskService taskService;

    @PostMapping("/task/create")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNewTask(
            @Valid @RequestBody CreateNewTaskDto dto,
            BindingResult bindingResult
    ) {
        log.info("Создание задачи: {}", dto);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err ->
                    errors.put(err.getField(), err.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            taskService.createNewTask(dto);
            // возвращаем единый JSON
            return ResponseEntity.ok(
                    Collections.singletonMap("message", "Task created successfully")
            );
        } catch (Exception e) {
            log.error("Ошибка при сохранении задачи", e);
            return ResponseEntity
                    .status(500)
                    .body(
                            Collections.singletonMap(
                                    "message",
                                    "Internal server error: " + e.getMessage()
                            )
                    );
        }
    }
}
