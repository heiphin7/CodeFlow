package com.api.codeflow.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Table(name = "submissions")
@Entity
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    private String language;
    private String status;
    private Double executionTime;
    private Double memoryUsage;
    private Date createdAt;

    @Lob
    private String code;

    // Нужно ли тут привязывать к пользователю??
}
