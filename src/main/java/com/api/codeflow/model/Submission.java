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
    private String status;             // "Accepted", "Wrong Answer", "Compilation Error", etc.
    private String errorType;          // TLE, WA, CE, RE, OOM, SUCCESS
    private Integer testCaseNumber;    // Номер теста, на котором упало (если не SUCCESS)
    private Double executionTime;
    private Double memoryUsage;
    private boolean isSuccess;
    private boolean isFinal;
    private String judgeRawStatus;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String code;

    @Lob
    private String resultOutput;

    @Lob
    private String compileOutput;

    @Lob
    private String stderr;
}

