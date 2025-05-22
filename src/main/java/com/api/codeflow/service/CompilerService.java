package com.api.codeflow.service;

import com.api.codeflow.dto.request.SubmitCodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilerService {

    private final TaskService taskService;

    public void checkSolution(Long taskId, SubmitCodeDto dto) {
        // Code here
    }
}
