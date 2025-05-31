package com.api.codeflow.service;

import com.api.codeflow.dto.LastSubmissionsDto;
import com.api.codeflow.dto.ShortSubmissionDto;
import com.api.codeflow.dto.SubmissionDto;
import com.api.codeflow.dto.TaskSubmissionDto;
import com.api.codeflow.jwt.JwtTokenUtils;
import com.api.codeflow.model.Submission;
import com.api.codeflow.model.Task;
import com.api.codeflow.model.User;
import com.api.codeflow.repository.SubmissionRepository;
import com.api.codeflow.repository.TaskRepository;
import com.api.codeflow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

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

    @Transactional
    public List<TaskSubmissionDto> getTaskSubmissions(Long taskId, HttpServletRequest request) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User in request not found!"));

        Task task = taskRepository.findById(taskId).orElse(null);

        // Получаем последние 10 сабмитов пользователя по задаче
        List<Submission> submissions = submissionRepository.findTop10ByUserAndTaskOrderByCreatedAtDesc(user, task);

        List<TaskSubmissionDto> result = new ArrayList<>();
        for (Submission s : submissions) {
            TaskSubmissionDto dto = new TaskSubmissionDto();
            dto.setStatus(s.getStatus());
            dto.setLanguage(s.getLanguage());
            dto.setMemoryUsage(s.getMemoryUsage());
            dto.setTimeUsage(s.getExecutionTime());
            dto.setCode(s.getCode());  // <-- здесь был вылет
            dto.setSubmittedTime(s.getCreatedAt());
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public List<LastSubmissionsDto> getAllSubmissions(HttpServletRequest request, int page, int size) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Submission> pageResult = submissionRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return pageResult.stream().map(sub -> {
            LastSubmissionsDto dto = new LastSubmissionsDto();
            dto.setId(sub.getId());
            dto.setTaskTitle(sub.getTask().getTitle());
            dto.setUsername(user.getUsername());
            dto.setMemoryUsage(sub.getMemoryUsage());
            dto.setTimeUsage(sub.getExecutionTime());
            dto.setStatus(sub.getStatus());
            dto.setTestCaseNumber(sub.getTestCaseNumber());
            dto.setCode(sub.getCode());
            dto.setLanguage(sub.getLanguage());
            dto.setUploadTime(sub.getCreatedAt());
            return dto;
        }).toList();
    }


}
