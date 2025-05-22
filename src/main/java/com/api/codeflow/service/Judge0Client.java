package com.api.codeflow.service;

import com.api.codeflow.dto.judge0.*;
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
    private static final String SUBMIT_URL = "http://localhost:2358/submissions/batch?base64_encoded=false";
    private static final String RESULT_URL = "http://localhost:2358/submissions/batch?tokens=";

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

            // Если все завершены (не в очереди/обработке)
            boolean allFinished = subs.stream()
                    .allMatch(r -> r.getStatus().getId() > 2);

            // Если хотя бы один не Accepted (ошибка, WA, CE, и т.д.) — сразу выходим
            boolean anyFailed = subs.stream()
                    .anyMatch(r -> r.getStatus().getId() > 2 && r.getStatus().getId() != 3);

            if (allFinished || anyFailed) {
                break;
            }

            Thread.sleep(200); // чуть быстрее, чем 500 мс
        }

        for (int i = 0; i < subs.size(); i++) {
            BatchSubmissionResult r = subs.get(i);
            log.info("Final result #{} — status={} stdout={} stderr={} compile_output={}",
                    i,
                    r.getStatus().getId(),
                    r.getStdout(),
                    r.getStderr(),
                    r.getCompile_output());
        }

        return subs;
    }
}


// TODO: Сейчас мы ждем пока все не закончатся
