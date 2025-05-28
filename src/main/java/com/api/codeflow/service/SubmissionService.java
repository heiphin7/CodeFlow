package com.api.codeflow.service;

import com.api.codeflow.dto.ShortSubmissionDto;
import com.api.codeflow.dto.SubmissionDto;
import com.api.codeflow.jwt.JwtTokenUtils;
import com.api.codeflow.model.Submission;
import com.api.codeflow.model.User;
import com.api.codeflow.repository.SubmissionRepository;
import com.api.codeflow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;

    @Transactional
    public List<ShortSubmissionDto> getLastSubmissions(HttpServletRequest request) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return Collections.emptyList();
        }

        // Ограничиваем до 5
        Pageable topFive = PageRequest.of(0, 5);
        List<Submission> submissions = submissionRepository.findRecentByUser(user, topFive);

        return submissions.stream().map(sub -> {
            ShortSubmissionDto dto = new ShortSubmissionDto();
            dto.setId(sub.getId());
            dto.setStatus(sub.getStatus());
            dto.setTaskName(sub.getTask().getTitle());
            return dto;
        }).toList();
    }


    public Page<SubmissionDto> findAllSubmissionsForUser(HttpServletRequest request, int page, int size) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Submission> submissionPage = submissionRepository.findAllByUser(user, pageable);

        return submissionPage.map(sub -> {
            SubmissionDto dto = new SubmissionDto();
            dto.setId(sub.getId());
            dto.setLanguage(sub.getLanguage());
            dto.setStatus(sub.getStatus());
            dto.setExecutionTime(sub.getExecutionTime());
            dto.setMemoryUsage(sub.getMemoryUsage());
            dto.setCreatedAt(sub.getCreatedAt());
            dto.setTaskTitle(sub.getTask().getTitle());
            return dto;
        });
    }

}
