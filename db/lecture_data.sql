-- Lecture 테이블 데이터 삽입 (계속)
INSERT INTO lecture (instructor_id, title, description, game_type, price, image_url, duration, game_tier, game_position, is_active)
VALUES
-- VALORANT 강의 (계속)
(46, 'IGL을 위한 옵저버 훈련', '오더, 라운드 플랜, 유틸 커맨드 집중', 'VALORANT', 21000, NULL, 75, 'PLATINUM,DIAMOND', 'INITIATOR', TRUE),
(45, 'VALORANT 수비 전술가', '수비 시 포지션별 유틸 활용 교육', 'VALORANT', 18000, NULL, 70, 'GOLD,PLATINUM', 'SENTINEL', TRUE),
(36, 'VALORANT 초보자를 위한 입문 강의', '밸로란트를 처음 시작하는 분들을 위한 기초 강의', 'VALORANT', 15000, NULL, 60, 'IRON,BRONZE', 'ALL', TRUE),
(36, 'VALORANT 에임 마스터하기', '정확한 조준 능력 향상을 위한 훈련법', 'VALORANT', 20000, NULL, 75, 'SILVER,GOLD', 'DUELIST', TRUE),
(45, 'VALORANT 맵 공략법', '각 맵별 특성과 효과적인 공략 전략', 'VALORANT', 17000, NULL, 65, 'SILVER,GOLD', 'ALL', TRUE),
(46, 'VALORANT 유틸리티 활용법', '각 요원별 스킬 활용의 최적화 방법', 'VALORANT', 16500, NULL, 60, 'SILVER,GOLD', 'ALL', TRUE),

-- TFT(롤체) 강의
(37, 'TFT 메타 전략', '리롤/운용 집중 강의', 'TFT', 16000, NULL, 55, 'GOLD,PLATINUM', NULL, TRUE),
(37, 'TFT 오픈 포트 전략의 핵심', '초반 손해를 감수하고 이기는 흐름', 'TFT', 19500, NULL, 70, 'PLATINUM,DIAMOND', NULL, TRUE),
(37, 'TFT 초반 운영 마스터', '2-1~3-2까지 이기는 흐름 만들기', 'TFT', 15500, NULL, 60, 'SILVER,GOLD', NULL, TRUE),
(37, 'TFT 아이템 우선순위 가이드', '어떤 아이템이 언제 강한지 분석', 'TFT', 15500, NULL, 55, 'SILVER,GOLD', NULL, TRUE),
(37, 'TFT 아이템 선택 전략', 'AP vs AD 상황별 판단 기준 제공', 'TFT', 16500, NULL, 55, 'GOLD,PLATINUM', NULL, TRUE),
(47, 'TFT 오픈 포트 전략 마스터', '고수들이 쓰는 오픈 전략 분석', 'TFT', 19000, NULL, 70, 'PLATINUM,DIAMOND', NULL, TRUE),
(48, 'TFT 초보자용 천천히 배워요', '배치 탈출을 위한 완전 기초 강의', 'TFT', 15000, NULL, 50, 'IRON,BRONZE', NULL, TRUE),
(48, 'TFT 완전 초보자용 튜토리얼', 'UI 조작부터 메타 기본 빌드까지', 'TFT', 14000, NULL, 55, 'IRON,BRONZE', NULL, TRUE),
(48, 'TFT 유닛 배치 기초', '초보자를 위한 자리잡기 기본', 'TFT', 13500, NULL, 50, 'IRON,BRONZE', NULL, TRUE),
(47, 'TFT 최종 1등 전략', '스테이지 별 우승 포인트 집중 공략', 'TFT', 23000, NULL, 70, 'PLATINUM,DIAMOND', NULL, TRUE),
(47, 'TFT 유닛 리롤 타이밍 가이드', '8연패, 3연승 등 상황별 리롤 분석', 'TFT', 18000, NULL, 70, 'PLATINUM,DIAMOND', NULL, TRUE),
(48, 'TFT 초보 탈출 전략', '4코어 중심 빌드업 전략', 'TFT', 15000, NULL, 60, 'BRONZE,SILVER', NULL, TRUE),

-- LOL 기타 강의
(34, 'LOL 초보자용 컨트롤 훈련', '시야 확보, 백핑 활용, 팀파이트 기초', 'LOL', 13000, NULL, 55, 'IRON,BRONZE', 'SUPPORT', TRUE),
(38, 'LOL 1:1 솔로 피드백 세션', '리플레이 기반 실전 피드백 중심 수업', 'LOL', 22000, NULL, 75, 'SILVER,GOLD', 'MID', TRUE),
(38, 'LOL 멘탈 관리 & 팀 빌딩', '트롤 없는 팀 게임 만들기', 'LOL', 16000, NULL, 60, 'SILVER', 'ALL', TRUE),
(4, '서포터로 이기는 법', '시야, 이니시, 백업 타이밍 완전정복', 'LOL', 15000, NULL, 60, 'SILVER', 'SUPPORT', TRUE),
(9, '프로 준비생 맞춤 강의', '목표 설정, 경기 복기, 리플레이 분석', 'LOL', 22000, NULL, 90, 'MASTER,GRANDMASTER', 'ALL', TRUE),
(9, 'LOL 팀랭 운영 & 밴픽 전략', '팀플레이 승률을 높이는 운영 전략', 'LOL', 22000, NULL, 80, 'GOLD,PLATINUM', 'ALL', TRUE),
(10, 'LOL 실력 점검 + 피드백', '리플레이 분석 중심의 진단형 강의', 'LOL', 16000, NULL, 65, 'SILVER,GOLD', 'MID', TRUE),
(20, 'LOL 1:1 실전 솔로큐 전략', '골드 탈출을 위한 실전 중심 코칭', 'LOL', 22000, NULL, 80, 'GOLD', 'MID', TRUE),
(34, 'LOL 0부터 시작하는 완전 입문', '클라이언트 조작부터 첫 승리까지', 'LOL', 12000, NULL, 55, 'IRON', 'ALL', TRUE),
(39, 'LOL 입문자를 위한 강의 A to Z', '게임 용어, 상점, 와드 이해하기', 'LOL', 13000, NULL, 50, 'IRON', 'ALL', TRUE),
(19, 'LOL 설정 최적화 & HUD 팁', '시야 범위, 마우스 설정 등 세팅 강의', 'LOL', 11000, NULL, 45, 'IRON', 'ALL', TRUE),
(19, 'LOL 키보드 손 배치 훈련', '편한 손 배치로 컨트롤 향상하기', 'LOL', 11000, NULL, 50, 'IRON', 'ALL', TRUE),
(14, 'LOL 마우스 세팅 + 핑 활용', '초보자가 반드시 익혀야 할 설정 가이드', 'LOL', 12000, NULL, 45, 'IRON', 'ALL', TRUE),
(14, '초보자 대상 스킬 조합 훈련', '챔피언별 Q-E-W 활용 순서 연습', 'LOL', 14000, NULL, 55, 'IRON', 'ALL', TRUE),
(50, 'LOL 피드백과 리플레이 리뷰', '1:1 녹화 분석 중심의 개선 강의', 'LOL', 23000, NULL, 75, 'SILVER,GOLD', 'ALL', TRUE);
