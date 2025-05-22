package com.api.codeflow.repository;

import com.api.codeflow.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = "testCases")
    Optional<Task> findWithTestCasesById(Long id);
}
