package com.api.codeflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    private Integer number;

    @Column(columnDefinition = "TEXT")
    private String solution;

    // Ограничения по выполнению кода
    private Double timeLimit;    // в секундах
    private Integer memoryLimit; // в мегабайтах

    @ManyToOne
    private Difficulty difficulty;

    // TODO: ДОБАВИТЬ ПРОЦЕНТ РЕШИВШИХСЯ

    // Сигнатурные методы для каждого языка програмирования

    @Lob
    private String pythonSignature;
    @Lob
    private String javaSignature;
    @Lob
    private String cppSignature;
    @Lob
    private String cSignature;
    @Lob
    private String csharpSignature;
    @Lob
    private String javascriptSignature;

    @ManyToMany
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TestCase> testCases;
}
