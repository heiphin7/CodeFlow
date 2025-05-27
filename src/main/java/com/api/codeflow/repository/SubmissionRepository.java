package com.api.codeflow.repository;

import com.api.codeflow.model.Submission;
import com.api.codeflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findTop5ByUserOrderByCreatedAtDesc(User user);
}
