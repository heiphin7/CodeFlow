package com.api.codeflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Tag extends JpaRepository<Tag, Long> {
}
