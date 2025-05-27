package com.api.codeflow.service;

import com.api.codeflow.dto.ShortSubmissionDto;
import com.api.codeflow.jwt.JwtTokenUtils;
import com.api.codeflow.model.Submission;
import com.api.codeflow.model.User;
import com.api.codeflow.repository.SubmissionRepository;
import com.api.codeflow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;

    public List<ShortSubmissionDto> getLastSubmissions(HttpServletRequest request) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return Collections.emptyList();
        }

        List<Submission> submissions = submissionRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        return submissions.stream().map(sub -> {
            ShortSubmissionDto dto = new ShortSubmissionDto();
            dto.setId(sub.getId());
            dto.setStatus(sub.getStatus());
            dto.setTaskName(sub.getTask().getTitle()); // вытаскиваем название задачи
            return dto;
        }).toList();
    }

}
