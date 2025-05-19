package com.api.codeflow.service;

import com.api.codeflow.dto.CreateNewTaskDto;
import com.api.codeflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public void createNewTask(CreateNewTaskDto dto) {



        // TODO: Мы должны тут также проверять, типа вот он если присылает solution
        // TODO: Рабочий ли у него самого решение???
    }
}
