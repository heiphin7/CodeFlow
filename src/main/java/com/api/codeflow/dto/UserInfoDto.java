package com.api.codeflow.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfoDto {
    private String username;
    private String email;
    private String primaryLanguage;
    private Date joinedDate;
    private String location;
    private String githubLink;
    private String AverageDifficulty;
    private Date lastSeen;
    private Double successRate;
    private Integer thisMonthSubmissions;
    private Integer thisWeekSolvedProblems;
    private String peakDay;
    private Integer peakDaySolvedProblems;
    private String preferredLanguage;
}
