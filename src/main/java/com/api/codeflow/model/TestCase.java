package com.api.codeflow.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "task_cases")
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String input;
    private String exceptedOutput;
    private Integer testNumber;
}
