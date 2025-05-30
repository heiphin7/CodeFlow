package com.api.codeflow.service;

import com.api.codeflow.dto.judge0.*;
import com.api.codeflow.dto.response.WrongSolution;
import com.api.codeflow.exception.TimeLimitExceededException;
import com.api.codeflow.exception.WrongSolutionException;
import com.api.codeflow.model.TestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class Judge0Client {
    private static final String SUBMIT_URL = "http://85.198.89.97:2358/submissions/batch?base64_encoded=false";
    private static final String RESULT_URL = "http://85.198.89.97:2358/submissions/batch?tokens=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<String> submitBatch(List<SubmissionRequest> submissions) {
        // сериализуем и логируем
        String json;
        try {
            json = objectMapper.writeValueAsString(new BatchSubmissionRequest(submissions));
            log.info(">>> SUBMIT_URL   = {}", SUBMIT_URL);
            log.info(">>> JSON REQUEST = {}", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // на вход передаём строку и явный JSON-заголовок
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        // распаковываем ответ как массив
        ResponseEntity<BatchSubmissionToken[]> resp = restTemplate
                .exchange(
                        SUBMIT_URL,
                        HttpMethod.POST,
                        entity,
                        BatchSubmissionToken[].class
                );

        log.info(">>> HTTP STATUS   = {}", resp.getStatusCode());
        log.info(">>> TOKEN ARRAY   = {}", Arrays.toString(resp.getBody()));

        // собираем List<String> токенов
        return Arrays.stream(resp.getBody())
                .map(BatchSubmissionToken::getToken)
                .toList();
    }

    public List<BatchSubmissionResult> getBatchResults(List<String> tokens) throws InterruptedException {
        String joined = String.join(",", tokens);
        List<BatchSubmissionResult> subs;

        while (true) {
            ResponseEntity<BatchSubmissionResultResponse> response =
                    restTemplate.getForEntity(RESULT_URL + joined, BatchSubmissionResultResponse.class);
            subs = response.getBody().getSubmissions();

            // Если ни один не в очереди/в процессе — значит все Accepted, выходим
            boolean allDone = subs.stream()
                    .allMatch(r -> r.getStatus().getId() > 2);
            if (allDone) break;

            Thread.sleep(200);
        }

        return subs;
    }

    // Вспомогательный метод, чтобы достать номер теста из sortedCases в CompilerService
    private int tcIndex(int batchIndex) {
        // batchIndex это 0-based, а TestCase.getTestNumber() — ваш реальный номер
        return /*sortedCases.get(batchIndex).getTestNumber()*/ batchIndex+1;
    }

    // Вспомогательные билдеры WrongSolution для разных ошибок
    private WrongSolution buildCE(int testCaseNumber, String compileOutput) {
        WrongSolution w = new WrongSolution();
        w.setTestCaseNumber(testCaseNumber);
        w.setProgramOutput(compileOutput);
        return w;
    }
    private WrongSolution buildWA(int testCaseNumber, String stdout, String stderr, String message) {
        WrongSolution w = new WrongSolution();
        w.setTestCaseNumber(testCaseNumber);
        w.setProgramOutput(stdout != null ? stdout : (stderr != null ? stderr : message));
        return w;
    }
}
