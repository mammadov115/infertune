package com.infertune.infertune;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;


@RestController
public class ChatController {

    private static final String MODEL = "qwen2.5:0.5b";

    private final RestClient ollama = RestClient.create("http://localhost:11434");

    public record ChatRequest(String prompt) {}

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest req) {
        Map<String, Object> r = ollama.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", MODEL, "prompt", req.prompt(), "stream", false))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        double evalCount = ((Number) r.get("eval_count")).doubleValue();
        double evalSeconds = ((Number) r.get("eval_duration")).doubleValue() / 1e9;
        double totalMs = ((Number) r.get("total_duration")).doubleValue() / 1e6;

        return Map.of(
                "response", r.get("response"),
                "tokens_per_second", evalSeconds > 0 ? evalCount / evalSeconds : 0,
                "total_ms", totalMs
        );
    }
}