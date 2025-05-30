package com.api.codeflow.controller;

import com.api.codeflow.dto.SubmissionDto;
import com.api.codeflow.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/submissions")
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

    // Для главной страницы, точнее для блока Recent Activity
    @GetMapping("/get/lastSubmissions")
    public ResponseEntity<?> getLastSubmissions(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(submissionService.getLastSubmissions(request));
        } catch (Exception e) {
            log.error("Error while fetching last submissions: " + e.getMessage());
            log.error("Stack trace: " , e);
            return new ResponseEntity<>("Server error :(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Для страница профиля
    @GetMapping("/findAll/{userId}")
    public ResponseEntity<?> getAllSubmissions(@PathVariable Long userId) {

    }

    // Для страницы решения задачи
    @GetMapping("/get/taskSubmissions/{taskId}")
    public ResponseEntity<?> getTaskSubmissions(@PathVariable Long taskId, HttpServletRequest) {

    }

}
