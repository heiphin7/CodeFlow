package com.api.codeflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisResultDto {
    private double overallScore;
    private double executionPerformance;
    private double languageAppropriateness;
    private double codeClarity;
    private double solutionCorrectness;
    private double modularity;

    private String aiSummary;

    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
}
