package com.api.codeflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Table(name = "task_solutions")
@Entity
public class TaskSolution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    private Task task;

    private String language;

    private Double executionTime; // в секундах

    private Double memoryUsage; // в мегабайтах

    @Temporal(TemporalType.TIMESTAMP)
    private Date solvedAt;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String code;
}
