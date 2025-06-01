package com.api.codeflow.repository;

import com.api.codeflow.model.Task;
import com.api.codeflow.model.TaskSolution;
import com.api.codeflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskSolutionRepository extends JpaRepository<TaskSolution, Long> {
    boolean existsByUserAndTask(User user, Task task);

    @Query("SELECT ts.task.id FROM TaskSolution ts WHERE ts.user = :user")
    List<Long> findSolvedTaskIdsByUser(@Param("user") User user);

    Optional<TaskSolution> findByUserAndTask(User user, Task task);
}
