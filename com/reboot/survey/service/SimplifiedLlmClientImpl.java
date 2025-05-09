package com.reboot.survey.service;

import com.reboot.survey.dto.LlmRequest;
import com.reboot.survey.dto.LlmResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SimplifiedLlmClientImpl implements LlmClient {

    private final Logger log = LoggerFactory.getLogger(SimplifiedLlmClientImpl.class);

    @Override
    public LlmResponse generateCompletion(LlmRequest request) {
        log.info("SimplifiedLLM 완성 요청: 최대 토큰 {}, 온도 {}",
                request.getMaxTokens(), request.getTemperature());

        // 실제 구현에서는 외부 LLM API(예: OpenAI, Anthropic)를 호출할 것
        // 개발 단계에서는 테스트를 위한 더미 응답 생성
        LlmResponse response = new LlmResponse();

        // 프롬프트에서 사용자 닉네임 추출 시도
        String userNickname = extractNicknameFromPrompt(request.getUserPrompt());
        
        // 요청한 포맷에 따라 예시 응답 생성
        String completion = String.format(
                "%s님을 위한 맞춤 강의 추천\n\n" +
                "1. [강의 ID: 1] LOL 초보자를 위한 기본기 마스터하기\n" +
                "   강사: 김프로 (전 프로게이머, 5년 경력)\n" +
                "   추천 이유: 초보 탈출을 목표로 하는 회원님에게 최적화된 기본기 강의입니다.\n" +
                "   기대 효과: 게임 메카닉 이해와 기본 조작 숙련도 향상\n\n" +
                "2. [강의 ID: 2] 정글링 마스터클래스\n" +
                "   강사: 박정글 (현역 코치, 챌린저 랭크)\n" +
                "   추천 이유: 정글 포지션에 특화된 강의로 맵 활용과 갱킹 전략을 배울 수 있습니다.\n" +
                "   기대 효과: 정글링 효율성 향상과 게임 영향력 증가\n\n" +
                "3. [강의 ID: 3] 미드라인 지배력 강화\n" +
                "   강사: 이미드 (전 프로게이머, 월드챔피언십 참가)\n" +
                "   추천 이유: 미드 포지션에서 라인 지배력을 높이기 위한 실전적인 강의입니다.\n" +
                "   기대 효과: 레이닝 페이즈 주도권 확보와 로밍 효율 증가\n\n" +
                "4. [강의 ID: 4] 팀파이트 포지셔닝 마스터\n" +
                "   강사: 최팀장 (전략 분석가, 10년 경력)\n" +
                "   추천 이유: 팀파이트에서 최적의 포지셔닝 감각을 익히는 강의입니다.\n" +
                "   기대 효과: 생존력 증가와 팀 기여도 향상\n\n" +
                "5. [강의 ID: 5] 메타 분석과 챔피언 풀 최적화\n" +
                "   강사: 박메타 (게임 분석가, 프로팀 전략 코치)\n" +
                "   추천 이유: 현재 메타에 맞는 챔피언 선택과 운영법을 배울 수 있습니다.\n" +
                "   기대 효과: 메타 이해도 증가와 승률 향상\n\n" +
                "종합 추천 의견: %s님의 초보 탈출 목표와 1:1 강의 선호도를 고려했을 때, 기본기 향상에 중점을 둔 강의를 추천드립니다. 특히 1번 강의는 %s님의 현재 실력 수준과 목표에 최적화되어 있습니다.",
                userNickname, userNickname, userNickname);

        response.setCompletion(completion);
        response.setModel("gpt-4");
        response.setPromptTokens(500);
        response.setCompletionTokens(800);

        log.info("LLM 완성 응답 생성 완료: {}자", completion.length());
        return response;
    }
    
    /**
     * 사용자 프롬프트에서 닉네임을 추출하려고 시도합니다.
     * 실제 프롬프트 구조에 맞게 수정되어야 합니다.
     */
    private String extractNicknameFromPrompt(String prompt) {
        // 프롬프트에서 닉네임 정보를 찾기
        if (prompt.contains("닉네임:")) {
            int start = prompt.indexOf("닉네임:") + "닉네임:".length();
            int end = prompt.indexOf("\n", start);
            if (end == -1) end = prompt.length();
            
            String nickname = prompt.substring(start, end).trim();
            if (!nickname.isEmpty()) {
                return nickname;
            }
        }
        
        // 기본값
        return "회원";
    }
}