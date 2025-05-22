package com.api.codeflow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

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

    @ManyToOne
    @JoinColumn(name = "task_id")
    @ToString.Exclude
    private Task task;
}
