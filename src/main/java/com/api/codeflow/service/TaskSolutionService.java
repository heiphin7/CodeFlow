package com.api.codeflow.service;

import com.api.codeflow.model.Task;
import com.api.codeflow.model.TaskSolution;
import com.api.codeflow.model.User;
import com.api.codeflow.repository.TaskSolutionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskSolutionService {

    private final TaskSolutionRepository taskSolutionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdateIfBetter(User user, Task task, String language, double execTime, double memory, String code) {
        Optional<TaskSolution> existingOpt = taskSolutionRepository.findByUserAndTask(user, task);

        if (existingOpt.isEmpty()) {
            TaskSolution solution = new TaskSolution();
            solution.setUser(user);
            solution.setTask(task);
            solution.setLanguage(language);
            solution.setExecutionTime(execTime);
            solution.setMemoryUsage(memory);
            solution.setSolvedAt(new Date());
            solution.setCode(code);
            taskSolutionRepository.save(solution);
        } else {
            TaskSolution existing = existingOpt.get();
            if (memory < existing.getMemoryUsage() && execTime < existing.getExecutionTime()) {
                existing.setLanguage(language);
                existing.setExecutionTime(execTime);
                existing.setMemoryUsage(memory);
                existing.setSolvedAt(new Date());
                existing.setCode(code);
                taskSolutionRepository.save(existing);
            }
        }
    }
}
