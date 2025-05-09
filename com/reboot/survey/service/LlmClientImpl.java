package com.reboot.survey.service;

import com.reboot.survey.dto.LlmRequest;
import com.reboot.survey.dto.LlmResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LlmClientImpl implements LlmClient {

    private final Logger log = LoggerFactory.getLogger(LlmClientImpl.class);

    @Override
    public LlmResponse generateCompletion(LlmRequest request) {
        log.info("LLM 완성 요청: 최대 토큰 {}, 온도 {}",
                request.getMaxTokens(), request.getTemperature());

        // 실제 구현에서는 외부 LLM API(예: OpenAI, Anthropic)를 호출할 것
        // 현재는 테스트를 위한 더미 응답 생성
        LlmResponse response = new LlmResponse();

        // 프롬프트에서 사용자 닉네임 추출 시도
        String userNickname = extractNicknameFromPrompt(request.getUserPrompt());
        
        // 요청한 포맷에 따라 예시 응답 생성
        String completion = String.format(
                "%s님을 위한 맞춤 강의 추천\n\n" +
                "1. [강의 ID: 5] LOL 입문자 탈출\n" +
                "   강사: 정코치 (입문 특화 코치)\n" +
                "   추천 이유: %s님의 초보자 실력 수준과 초보 탈출 목표에 가장 적합한 기초 강의입니다.\n" +
                "   기대 효과: 기본 UI, 설정, CS, 라인 유지 기초를 익혀 초보자에서 벗어날 수 있습니다.\n\n" +
                "2. [강의 ID: 34] LOL 0부터 시작하는 완전 입문\n" +
                "   강사: 황코치 (초보자 눈높이 교사)\n" +
                "   추천 이유: 클라이언트 조작부터 첫 승리까지의 과정을 상세히 안내하는 강의로, %s님처럼 게임을 처음 시작하는 분들에게 이상적입니다.\n" +
                "   기대 효과: 게임의 기본적인 흐름과 조작법을 완전히 이해하여 자신감 있게 플레이할 수 있습니다.\n\n" +
                "3. [강의 ID: 49] LOL 설정 최적화 & HUD 팁\n" +
                "   강사: 정코치 (LOL 입문 강의 재정의)\n" +
                "   추천 이유: 시야 범위, 마우스 설정 등 게임 환경 최적화를 통해 %s님의 게임 경험을 크게 향상시킬 수 있습니다.\n" +
                "   기대 효과: 최적화된 게임 설정으로 인터페이스를 더 효율적으로 사용하고 더 나은 결정을 내릴 수 있습니다.\n\n" +
                "종합 추천 의견: %s님은 LOL을 처음 접하시는 초보자이므로, 기본기와 게임 인터페이스 이해에 집중하는 강의를 추천드립니다. 특히 '입문자 탈출' 강의는 초보 탈출이라는 목표에 딱 맞는 단계별 접근법을 제공합니다.",
                userNickname, userNickname, userNickname, userNickname, userNickname);

        response.setCompletion(completion);
        response.setModel("gpt-4");
        response.setPromptTokens(500);
        response.setCompletionTokens(800);

        log.info("LLM 완성 응답 생성 완료: {}자", completion.length());
        return response;
    }
    
    /**
     * 사용자 프롬프트에서 닉네임을 추출하려고 시도합니다.
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
        
        // 회원 정보 섹션에서 찾기 시도
        if (prompt.contains("[회원 정보]")) {
            int start = prompt.indexOf("[회원 정보]");
            int end = prompt.indexOf("\n\n", start);
            if (end == -1) end = prompt.length();
            
            String memberInfo = prompt.substring(start, end);
            
            // 정보 내에서 닉네임 찾기
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
        
        // 기본값
        return "회원";
    }
    
    /**
     * 게임 타입에 따라 다른 추천 응답을 생성합니다.
     * 실제 구현에서는 이 부분이 LLM에 의해 처리될 것입니다.
     */
    private String generateDummyResponseForGameType(String gameType, String userNickname) {
        // 각 게임 타입별 추천 템플릿 구현
        // 실제로는 이 로직이 LLM에 의해 대체됨
        switch (gameType) {
            case "LOL":
                return generateLolRecommendation(userNickname);
            case "VALORANT":
                return generateValorantRecommendation(userNickname);
            case "TFT":
                return generateTftRecommendation(userNickname);
            default:
                return generateDefaultRecommendation(userNickname);
        }
    }
    
    private String generateLolRecommendation(String userNickname) {
        return String.format(
                "%s님을 위한 맞춤 LOL 강의 추천\n\n" +
                "1. [강의 ID: 5] LOL 입문자 탈출\n" +
                "   강사: 정코치 (입문 특화 코치)\n" +
                "   추천 이유: %s님의 초보자 실력 수준과 초보 탈출 목표에 가장 적합한 기초 강의입니다.\n" +
                "   기대 효과: 기본 UI, 설정, CS, 라인 유지 기초를 익혀 초보자에서 벗어날 수 있습니다.\n\n" +
                "2. [강의 ID: 34] LOL 0부터 시작하는 완전 입문\n" +
                "   강사: 황코치 (초보자 눈높이 교사)\n" +
                "   추천 이유: 클라이언트 조작부터 첫 승리까지의 과정을 상세히 안내하는 강의입니다.\n" +
                "   기대 효과: 게임의 기본적인 흐름과 조작법을 완전히 이해하여 자신감 있게 플레이할 수 있습니다.\n\n" +
                "3. [강의 ID: 49] LOL 설정 최적화 & HUD 팁\n" +
                "   강사: 정코치 (LOL 입문 강의 재정의)\n" +
                "   추천 이유: 시야 범위, 마우스 설정 등 게임 환경 최적화를 통해 게임 경험을 크게 향상시킵니다.\n" +
                "   기대 효과: 최적화된 게임 설정으로 인터페이스를 더 효율적으로 사용하고 더 나은 결정을 내릴 수 있습니다.\n\n" +
                "종합 추천 의견: %s님은 LOL을 처음 접하시는 초보자이므로, 기본기와 게임 인터페이스 이해에 집중하는 강의를 추천드립니다.",
                userNickname, userNickname, userNickname);
    }
    
    private String generateValorantRecommendation(String userNickname) {
        return String.format(
                "%s님을 위한 맞춤 VALORANT 강의 추천\n\n" +
                "1. [강의 ID: 25] VALORANT 피킹 타이밍과 크로스헤어\n" +
                "   강사: 류코치 (VALORANT 수비 전술가)\n" +
                "   추천 이유: 1인칭 슈팅의 기본을 다지기 위한 기초 강의로 에임 훈련에 집중합니다.\n" +
                "   기대 효과: 정확한 조준과 적절한 피킹 타이밍으로 교전 승률을 높일 수 있습니다.\n\n" +
                "2. [강의 ID: 6] 발로란트 AIM 훈련\n" +
                "   강사: 윤코치 (발로란트 에이밍 전문)\n" +
                "   추천 이유: 헤드샷 확률을 높이기 위한 집중 훈련으로 에임 실력을 크게 향상시킵니다.\n" +
                "   기대 효과: 정교한 조준으로 교전에서 승리할 확률이 높아집니다.\n\n" +
                "3. [강의 ID: 45] VALORANT 포지션별 팁\n" +
                "   강사: 류코치 (VALORANT 수비 전술가)\n" +
                "   추천 이유: 각 포지션별 역할과 로테이션을 이해하여 팀 플레이 능력을 향상시킵니다.\n" +
                "   기대 효과: 자신의 포지션을 더 잘 이해하고 팀에 기여할 수 있습니다.\n\n" +
                "종합 추천 의견: %s님은 VALORANT 실력 향상을 목표로 하고 있으므로, 에임 훈련과 포지션 이해에 중점을 둔 강의를 추천드립니다.",
                userNickname, userNickname);
    }
    
    private String generateTftRecommendation(String userNickname) {
        return String.format(
                "%s님을 위한 맞춤 TFT 강의 추천\n\n" +
                "1. [강의 ID: 38] TFT 완전 초보자용 튜토리얼\n" +
                "   강사: 강코치 (TFT 초보자용 천천히 배워요 코치)\n" +
                "   추천 이유: UI 조작부터 메타 기본 빌드까지 완전 초보자를 위한 가이드를 제공합니다.\n" +
                "   기대 효과: 게임의 기본 메커니즘을 이해하고 기초적인 전략을 배울 수 있습니다.\n\n" +
                "2. [강의 ID: 17] TFT 초보 탈출 전략\n" +
                "   강사: 장코치 (TFT 고랭커)\n" +
                "   추천 이유: 4코어 중심 빌드업 전략으로 브론즈에서 벗어나는 방법을 배울 수 있습니다.\n" +
                "   기대 효과: 안정적인 빌드로 하위 티어에서 탈출할 수 있습니다.\n\n" +
                "3. [강의 ID: 48] TFT 유닛 배치 기초\n" +
                "   강사: 강코치 (TFT 초보자용 천천히 배워요 코치)\n" +
                "   추천 이유: 초보자를 위한 자리잡기 기본 강의로 전투력을 극대화하는 방법을 배웁니다.\n" +
                "   기대 효과: 효율적인 유닛 배치로 전투 승률을 높일 수 있습니다.\n\n" +
                "종합 추천 의견: %s님은 TFT를 처음 접하시는 초보자이므로, 기본 메커니즘과 빌드업 전략에 중점을 둔 강의를 추천드립니다.",
                userNickname, userNickname);
    }
    
    private String generateDefaultRecommendation(String userNickname) {
        return String.format(
                "%s님을 위한 맞춤 강의 추천\n\n" +
                "1. [강의 ID: 5] LOL 입문자 탈출\n" +
                "   강사: 정코치 (입문 특화 코치)\n" +
                "   추천 이유: 초보자 실력 수준과 초보 탈출 목표에 가장 적합한 기초 강의입니다.\n" +
                "   기대 효과: 기본 UI, 설정, CS, 라인 유지 기초를 익혀 초보자에서 벗어날 수 있습니다.\n\n" +
                "2. [강의 ID: 25] VALORANT 피킹 타이밍과 크로스헤어\n" +
                "   강사: 류코치 (VALORANT 수비 전술가)\n" +
                "   추천 이유: 1인칭 슈팅의 기본을 다지기 위한 기초 강의로 에임 훈련에 집중합니다.\n" +
                "   기대 효과: 정확한 조준과 적절한 피킹 타이밍으로 교전 승률을 높일 수 있습니다.\n\n" +
                "3. [강의 ID: 38] TFT 완전 초보자용 튜토리얼\n" +
                "   강사: 강코치 (TFT 초보자용 천천히 배워요 코치)\n" +
                "   추천 이유: UI 조작부터 메타 기본 빌드까지 완전 초보자를 위한 가이드를 제공합니다.\n" +
                "   기대 효과: 게임의 기본 메커니즘을 이해하고 기초적인 전략을 배울 수 있습니다.\n\n" +
                "종합 추천 의견: %s님의 게임 실력 향상을 위해 다양한 게임의 기초 강의를 추천드립니다. 관심 있는 게임을 선택하여 시작해보세요.",
                userNickname, userNickname);
    }
}