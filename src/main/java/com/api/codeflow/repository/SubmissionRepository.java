package com.api.codeflow.repository;

import com.api.codeflow.model.Submission;
import com.api.codeflow.model.Task;
import com.api.codeflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findTop5ByUserOrderByCreatedAtDesc(User user);
    Page<Submission> findAllByUser(User user, Pageable pageable);
    @Query("SELECT s FROM Submission s JOIN FETCH s.task WHERE s.user = :user ORDER BY s.createdAt DESC")
    List<Submission> findRecentByUser(@Param("user") User user, Pageable pageable);

    List<Submission> findTop10ByUserAndTaskOrderByCreatedAtDesc(User user, Task task);
    Page<Submission> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
