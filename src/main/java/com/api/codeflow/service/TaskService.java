package com.api.codeflow.service;

import com.api.codeflow.dto.CreateNewTaskDto;
import com.api.codeflow.dto.TaskInfoDto;
import com.api.codeflow.exception.NotFoundException;
import com.api.codeflow.jwt.JwtTokenUtils;
import com.api.codeflow.model.*;
import com.api.codeflow.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final DifficultyRepository difficultyRepository;
    private final TagRepository tagRepository;
    private final TestCaseRepository testCaseRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;
    private final TaskSolutionRepository taskSolutionRepository;

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

    public TaskInfoDto findTaskById(Long taskId, HttpServletRequest request) throws NotFoundException {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new NotFoundException("Task with id=" + taskId + " not founded!")
        );

        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username).orElse(null);

        List<String> tags = task.getTags().stream()
                .map(Tag::getName)
                .toList();

        // Проверяем через репозиторий (надёжно и быстро)
        boolean isSolved = false;
        if (user != null) {
            isSolved = taskSolutionRepository.existsByUserAndTask(user, task);
        }

        // Маппим в DTO
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
        dto.setIsSolved(isSolved);

        return dto;
    }

    public List<TaskInfoDto> findAllTasks(HttpServletRequest request) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username).orElse(null);

        List<Task> tasks = taskRepository.findAll();
        List<TaskInfoDto> dtos = new ArrayList<>();

        Set<Long> solvedTaskIdSet = new HashSet<>();
        if (user != null) {
            List<Long> solvedTaskIds = taskSolutionRepository.findSolvedTaskIdsByUser(user);
            solvedTaskIdSet = new HashSet<>(solvedTaskIds); // используем Set для быстрого contains()
        }

        for (Task task : tasks) {
            TaskInfoDto dto = new TaskInfoDto();

            List<String> tags = task.getTags().stream()
                    .map(Tag::getName)
                    .toList();

            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setNumber(task.getNumber());
            dto.setSolution(task.getSolution());
            dto.setTags(tags);
            dto.setTimeLimit(task.getTimeLimit());
            dto.setMemoryLimit(task.getMemoryLimit());
            dto.setDifficulty(task.getDifficulty().getName());

            dto.setIsSolved(solvedTaskIdSet.contains(task.getId()));

            dtos.add(dto);
        }

        return dtos;
    }




    @Transactional(readOnly = true)
    public Task findByIdWithTestCases(Long taskId) {
        return taskRepository.findWithTestCasesById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task with ID " + taskId + " not found"));
    }
}
