package com.api.codeflow.controller;

import com.api.codeflow.dto.CreateNewTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private

    @PostMapping("/task/create")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNewTask(@RequestBody CreateNewTaskDto dto) {
        try {

        } catch () {

        } catch (Exception e) {

        }
    }
}
