package com.api.codeflow.service;

import com.api.codeflow.dto.AnalysisResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    @Value("${rapidApiKey}")
    private String apiKey;
    private final String model = "meta-llama/llama-3.3-8b-instruct:free";
    private final String path = "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisResultDto getAiAnalysis(String solution, String taskDescription, Double time, Double memory) {
        try {
            // Создаём тело запроса
            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", buildPrompt(solution, taskDescription, memory, time)
            );

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(message)
            );

            // Заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Выполняем запрос
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    path,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            String rawContent = response
                    .getBody()
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            // Удаляем обёртки ```json ... ``` или просто ```...```
            String cleanedContent = rawContent
                    .replaceAll("(?s)^```json\\s*", "") // убирает ```json
                    .replaceAll("(?s)^```\\s*", "")     // убирает ```
                    .replaceAll("(?s)```$", "")         // убирает завершающее ```
                    .trim();

            log.info("🧪 Cleaned AI JSON:\n{}", cleanedContent);

            // Десериализуем
            return objectMapper.readValue(cleanedContent, AnalysisResultDto.class);


        } catch (Exception e) {
            log.error("AI analysis request failed", e);
            return null;
        }
    }

    private String buildPrompt(String solution, String taskDescription, Double memory, Double time) {
        return """
        You are an expert programming mentor and code reviewer.

        Your task is to evaluate the given solution to a programming task using well-defined criteria and return the result in valid JSON only (no markdown, no formatting, no extra commentary outside the JSON).

        Follow these strict evaluation rules:

        ✅ Solution Correctness (0–10):
        - Give 10/10 ONLY if the logic is fully correct and no bugs or unnecessary constructs are present.
        - DO NOT lower the score for correct and clean implementations with zero flaws.

        ✅ Execution Performance (0–10):
        - Give 10/10 if runtime is <10ms and memory is <1MB.
        - Do NOT reduce the score unless performance is noticeably suboptimal due to actual inefficiencies.

        ✅ Modularity (0–10):
        - For simple tasks with a single `solve()` function or small decomposition, give 10/10.
        - Reduce only when the task clearly requires functional separation, but it’s not implemented.

        ✅ Code Clarity (0–10):
        - Good naming, comments, and simple structure = 10/10.
        - DO NOT penalize if code is minimal, readable, and elegant.

        ✅ Language Appropriateness (0–10):
        - If the language is suitable for the problem domain, give 9–10.

        ⚠️ VERY IMPORTANT:
        - Do NOT be nitpicky.
        - Do NOT lower scores out of habit or by comparing to idealized academic style.
        - Only penalize when there is a clear issue: bad code structure, bad naming, bad performance, wrong logic, etc.

        🔍 AI Summary instructions:
        - The `aiSummary` must be concise (maximum 5–6 sentences).
        - Start with an overall evaluation of the code.
        - If any score is significantly lower than 10 (especially in the first 3–4 fields), clearly and briefly justify **why**.
        - If all values are high, you may simply acknowledge the quality of the solution.
        - Do not repeat information from `strengths`, just summarize.

        JSON structure:
        {
          "overallScore": ...,
          "executionPerformance": ...,
          "languageAppropriateness": ...,
          "codeClarity": ...,
          "solutionCorrectness": ...,
          "modularity": ...,
          "aiSummary": "...",
          "strengths": [...],
          "weaknesses": [...],
          "suggestions": [...]
        }

        Task:
        %s

        Solution:
        %s

        Memory Usage (in MB): %s
        Time Usage (in ms): %s
        """.formatted(taskDescription, solution, memory, time);
    }

}
