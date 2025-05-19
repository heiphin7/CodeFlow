package com.api.codeflow.controller;

import com.api.codeflow.dto.CreateNewTaskDto;
import com.api.codeflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private TaskService taskService;

    @PostMapping("/task/create")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNewTask(@Valid @RequestBody CreateNewTaskDto dto,
                                           BindingResult bindingResult) {
        // Проверяем, если есть ошибка при проверке от Hibernate Validator
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

    }
}
