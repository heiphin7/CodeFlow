package com.api.codeflow.service;

import com.api.codeflow.dto.CreateNewTaskDto;
import com.api.codeflow.dto.TaskInfoDto;
import com.api.codeflow.exception.NotFoundException;
import com.api.codeflow.model.Difficulty;
import com.api.codeflow.model.Tag;
import com.api.codeflow.model.Task;
import com.api.codeflow.model.TestCase;
import com.api.codeflow.repository.DifficultyRepository;
import com.api.codeflow.repository.TagRepository;
import com.api.codeflow.repository.TaskRepository;
import com.api.codeflow.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final DifficultyRepository difficultyRepository;
    private final TagRepository tagRepository;
    private final TestCaseRepository testCaseRepository;

    // TODO: Также если ошибка мы должны вернуть кароче dto чтобы он мог редактировать
    // TODO: А то если типа ошибка то тогда писать с нуля не самая хорошая идея

    // TODO: Мы должны тут также проверять, типа вот он если присылает solution
    // TODO: Рабочий ли у него самого решение???

    @Transactional
    public void createNewTask(CreateNewTaskDto dto) {
        Task task = new Task();

        // Сохраняем все тест кейсы
        testCaseRepository.saveAll(dto.getTestCases());

        // Сложность задачи
        Difficulty difficulty = difficultyRepository.findByName(dto.getDifficulty());

        if (difficulty == null) {
            difficulty = new Difficulty();
            difficulty.setName(dto.getDifficulty());
            difficultyRepository.save(difficulty);
        }

        List<Tag> tags = new ArrayList<>();
        for (String tagName: dto.getTags()) {
            Tag tagInDb = tagRepository.findByName(tagName);

            if (tagInDb == null) {
                tagInDb = new Tag();
                tagInDb.setName(tagName);
                tagRepository.save(tagInDb);
            }

            tags.add(tagInDb);
        }

        List<TestCase> testCases = dto.getTestCases();
        for (TestCase tc : testCases) {
            tc.setTask(task);
        }

        task.setTitle(dto.getTitle());
        task.setDifficulty(difficulty);
        task.setDescription(dto.getDescription());
        task.setNumber(dto.getNumber());
        task.setSolution(dto.getSolution());
        task.setTags(tags);
        task.setTimeLimit(dto.getTimeLimit());
        task.setTestCases(testCases);
        task.setMemoryLimit(dto.getMemoryLimit());

        taskRepository.save(task);
    }

    public TaskInfoDto findTaskById(Long taskId) throws NotFoundException {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new NotFoundException("Task with id=" + taskId + " not founded!")
        );

        List<String> tags = new ArrayList<>();
        for(Tag tag: task.getTags()) {
            tags.add(tag.getName());
        }

        // Маппим данные
        TaskInfoDto dto = new TaskInfoDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setNumber(task.getNumber());
        dto.setSolution(task.getSolution());
        dto.setTags(tags);
        dto.setTimeLimit(task.getTimeLimit());
        dto.setMemoryLimit(task.getMemoryLimit());
        dto.setDifficulty(task.getDifficulty().getName());

        return dto;
    }

    public List<TaskInfoDto> findAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        log.info("Tasks in db: " + tasks);
        List<TaskInfoDto> dtos = new ArrayList<>();

        for (Task task: tasks) {
            TaskInfoDto dto = new TaskInfoDto();

            List<String> tags = new ArrayList<>();
            for(Tag tag: task.getTags()) {
                tags.add(tag.getName());
            }
            // TODO: Make the mapper method or even class for Task -> TaskInfoDto???
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setNumber(task.getNumber());
            dto.setSolution(task.getSolution());
            dto.setTags(tags);
            dto.setTimeLimit(task.getTimeLimit());
            dto.setMemoryLimit(task.getMemoryLimit());
            dto.setDifficulty(task.getDifficulty().getName());

            dtos.add(dto);
        }

        return dtos;
    }
}
