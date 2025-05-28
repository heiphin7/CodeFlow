package com.api.codeflow.controller;

import com.api.codeflow.dto.SubmissionDto;
import com.api.codeflow.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/submissions")
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

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

    @GetMapping("/findAll")
    public ResponseEntity<?> getAllSubmissionsForUser(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<SubmissionDto> result = submissionService.findAllSubmissionsForUser(request, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error while fetching submissions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Server Error");
        }
    }
}
