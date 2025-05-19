package com.api.codeflow.service;

import com.api.codeflow.dto.CreateNewTaskDto;
import com.api.codeflow.repository.DifficultyRepository;
import com.api.codeflow.repository.TagRepository;
import com.api.codeflow.repository.TaskRepository;
import com.api.codeflow.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final DifficultyRepository difficultyRepository;
    private final TagRepository tagRepository;
    private final TestCaseRepository testCaseRepository;

    public void createNewTask(CreateNewTaskDto dto) {
        // TODO: Такие "Подмодели" как Tag, Difficulty, TestCase
        //  нужно будет проверять и создавать каждый раз

        // TODO: Также если ошибка мы должны вернуть кароче dto чтобы он мог редактировать
        // TODO: А то если типа ошибка то тогда писать с нуля не самая хорошая идея

        // TODO: Мы должны тут также проверять, типа вот он если присылает solution
        // TODO: Рабочий ли у него самого решение???
    }
}
