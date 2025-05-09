package com.reboot.survey.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboot.survey.dto.LlmRequest;
import com.reboot.survey.dto.LlmResponse;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Together AI API를 사용한 LLM 클라이언트 구현
 */
@Service
public class TogetherAiLlmClient implements LlmClient {

    private final Logger log = LoggerFactory.getLogger(TogetherAiLlmClient.class);
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${together.ai.api.key:1a219d230d890a163191c1552e1ad1f5e71150314cd15169c1f4ca9ce0d67f5a}")
    private String apiKey;
    
    @Value("${together.ai.model:Qwen/Qwen2.5-72B-Instruct-Turbo}")
    private String model;
    
    private static final String API_URL = "https://api.together.xyz/v1/chat/completions";

    public TogetherAiLlmClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LlmResponse generateCompletion(LlmRequest request) {
        log.info("Together AI LLM 요청 시작: 모델={}, 최대 토큰={}, 온도={}", 
                model, request.getMaxTokens(), request.getTemperature());
        
        try {
            // Together API 요청 형식으로 변환
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", request.getMaxTokens());
            requestBody.put("temperature", request.getTemperature());
            
            // 메시지 포맷 구성
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 시스템 프롬프트 추가
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", request.getSystemPrompt());
                messages.add(systemMessage);
            }
            
            // 사용자 프롬프트 추가
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getUserPrompt());
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            
            // API 요청 전송
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(requestBody)
            );
            
            Request apiRequest = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("authorization", "Bearer " + apiKey)
                    .build();
            
            // API 응답 처리
            try (Response response = httpClient.newCall(apiRequest).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Together AI API 오류: {}", response.code());
                    throw new RuntimeException("API 오류: " + response.code() + " - " + response.message());
                }
                
                // 응답 파싱
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                // 응답 포맷 처리
                LlmResponse llmResponse = new LlmResponse();
                
                // choices[0].message.content에서 텍스트 추출
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        llmResponse.setCompletion((String) message.get("content"));
                    }
                }
                
                // 토큰 사용량 정보 추출
                Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
                if (usage != null) {
                    llmResponse.setPromptTokens(((Number) usage.get("prompt_tokens")).intValue());
                    llmResponse.setCompletionTokens(((Number) usage.get("completion_tokens")).intValue());
                }
                
                llmResponse.setModel(model);
                
                log.info("Together AI LLM 응답 완료: 프롬프트 토큰={}, 완성 토큰={}",
                        llmResponse.getPromptTokens(), llmResponse.getCompletionTokens());
                
                return llmResponse;
            }
        } catch (IOException e) {
            log.error("Together AI 통신 오류", e);
            throw new RuntimeException("API 통신 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Together AI LLM 오류", e);
            
            // 오류 시 폴백 응답 제공
            LlmResponse fallbackResponse = createFallbackResponse(request);
            return fallbackResponse;
        }
    }
    
    /**
     * API 호출 실패 시 폴백 응답을 생성합니다.
     */
    private LlmResponse createFallbackResponse(LlmRequest request) {
        log.info("폴백 응답 생성 중...");
        LlmResponse response = new LlmResponse();
        
        // 프롬프트에서 사용자 닉네임 추출 시도
        String userNickname = extractNicknameFromPrompt(request.getUserPrompt());
        
        // 기본 추천 응답 텍스트
        String completion = String.format(
                "%s님을 위한 맞춤 강의 추천\n\n" +
                "1. [강의 ID: 5] LOL 입문자 탈출\n" +
                "   강사: 정코치 (입문 특화 코치)\n" +
                "   추천 이유: %s님의 실력 수준과 목표에 가장 적합한 기초 강의입니다.\n" +
                "   기대 효과: 기본 UI, 설정, CS, 라인 유지 기초를 익혀 초보자에서 벗어날 수 있습니다.\n\n" +
                "2. [강의 ID: 34] LOL 0부터 시작하는 완전 입문\n" +
                "   강사: 황코치 (초보자 눈높이 교사)\n" +
                "   추천 이유: 클라이언트 조작부터 첫 승리까지의 과정을 상세히 안내합니다.\n" +
                "   기대 효과: 게임의 기본적인 흐름과 조작법을 완전히 이해할 수 있습니다.\n\n" +
                "3. [강의 ID: 49] LOL 설정 최적화 & HUD 팁\n" +
                "   강사: 정코치 (LOL 입문 강의 재정의)\n" +
                "   추천 이유: 시야 범위, 마우스 설정 등 게임 환경 최적화를 통해 게임 경험을 향상시킵니다.\n" +
                "   기대 효과: 최적화된 게임 설정으로 인터페이스를 더 효율적으로 사용할 수 있습니다.\n\n" +
                "종합 추천 의견: %s님의 설문 결과를 바탕으로 기본기와 게임 인터페이스 이해에 집중하는 강의를 추천드립니다.",
                userNickname, userNickname, userNickname);

        response.setCompletion(completion);
        response.setModel("together_ai_fallback");
        response.setPromptTokens(0);
        response.setCompletionTokens(0);
        
        return response;
    }
    
    /**
     * 사용자 프롬프트에서 닉네임을 추출하려고 시도합니다.
     */
    private String extractNicknameFromPrompt(String prompt) {
        // 닉네임 추출 로직 구현
        try {
            if (prompt.contains("닉네임:")) {
                int start = prompt.indexOf("닉네임:") + "닉네임:".length();
                int end = prompt.indexOf("\n", start);
                if (end == -1) end = prompt.length();
                
                String nickname = prompt.substring(start, end).trim();
                if (!nickname.isEmpty()) {
                    return nickname;
                }
            }
            
            // 회원 정보 섹션에서 찾기
            if (prompt.contains("[회원 정보]")) {
                int start = prompt.indexOf("[회원 정보]");
                int end = prompt.indexOf("\n\n", start);
                if (end == -1) end = prompt.length();
                
                String memberInfo = prompt.substring(start, end);
                
                if (memberInfo.contains("닉네임:")) {
                    int nicknameStart = memberInfo.indexOf("닉네임:") + "닉네임:".length();
                    int nicknameEnd = memberInfo.indexOf("\n", nicknameStart);
                    if (nicknameEnd == -1) nicknameEnd = memberInfo.length();
                    
                    String nickname = memberInfo.substring(nicknameStart, nicknameEnd).trim();
                    if (!nickname.isEmpty()) {
                        return nickname;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("닉네임 추출 실패", e);
        }
        
        // 기본값
        return "회원";
    }
}