package com.api.codeflow.controller;

import com.api.codeflow.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/submissions")
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/get/lastSubmissions")
    public ResponseEntity<?> getLastSubmissions(HttpServletRequest httpServletRequest) {
        try {

        } catch () {

        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<?> getAllSubmissionsForUser(HttpServletRequest request) {
        try {

        } catch () {

        }
    }
}
