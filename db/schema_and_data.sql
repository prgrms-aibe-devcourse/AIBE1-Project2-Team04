-- Member 테이블 생성
CREATE TABLE member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255) NOT NULL UNIQUE,
    profile_image VARCHAR(255),
    phone VARCHAR(20),
    role VARCHAR(50)
) ENGINE=InnoDB;

-- Game 테이블 생성
CREATE TABLE game (
    game_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    game_type VARCHAR(255),
    game_tier VARCHAR(255),
    game_position VARCHAR(255),
    FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB;

-- Instructor 테이블 생성
CREATE TABLE instructor (
    instructor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    career VARCHAR(1000),
    description VARCHAR(2000),
    review_num INT DEFAULT 0,
    average_rating DOUBLE DEFAULT 0.0,
    created_at DATETIME,
    total_members INT DEFAULT 0,
    FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB;

-- 학생(MEMBER) 데이터 삽입
INSERT INTO member (username, password, name, email, nickname, profile_image, phone, role)
VALUES 
('student1', 'pw1', '김학생', 'student1@example.com', '학생킴', NULL, '01012345678', 'MEMBER'),
('student2', 'pw2', '이학생', 'student2@example.com', '학생이', NULL, '01023456789', 'MEMBER'),
('student3', 'pw3', '박학생', 'student3@example.com', '학생박', NULL, '01034567890', 'MEMBER'),
('student4', 'pw4', '최학생', 'student4@example.com', '학생최', NULL, '01045678901', 'MEMBER'),
('student5', 'pw5', '정학생', 'student5@example.com', '학생정', NULL, '01056789012', 'MEMBER'),
('student6', 'pw6', '강학생', 'student6@example.com', '학생강', NULL, '01067890123', 'MEMBER'),
('student7', 'pw7', '조학생', 'student7@example.com', '학생조', NULL, '01078901234', 'MEMBER'),
('student8', 'pw8', '윤학생', 'student8@example.com', '학생윤', NULL, '01089012345', 'MEMBER'),
('student9', 'pw9', '장학생', 'student9@example.com', '학생장', NULL, '01090123456', 'MEMBER'),
('student10', 'pw10', '임학생', 'student10@example.com', '학생임', NULL, '01001234567', 'MEMBER'),
('student11', 'pw11', '한학생', 'student11@example.com', '학생한', NULL, '01098765432', 'MEMBER'),
('student12', 'pw12', '오학생', 'student12@example.com', '학생오', NULL, '01087654321', 'MEMBER'),
('student13', 'pw13', '서학생', 'student13@example.com', '학생서', NULL, '01076543210', 'MEMBER'),
('student14', 'pw14', '신학생', 'student14@example.com', '학생신', NULL, '01065432109', 'MEMBER'),
('student15', 'pw15', '권학생', 'student15@example.com', '학생권', NULL, '01054321098', 'MEMBER'),
('student16', 'pw16', '황학생', 'student16@example.com', '학생황', NULL, '01043210987', 'MEMBER'),
('student17', 'pw17', '안학생', 'student17@example.com', '학생안', NULL, '01032109876', 'MEMBER'),
('student18', 'pw18', '송학생', 'student18@example.com', '학생송', NULL, '01021098765', 'MEMBER'),
('student19', 'pw19', '전학생', 'student19@example.com', '학생전', NULL, '01010987654', 'MEMBER'),
('student20', 'pw20', '홍학생', 'student20@example.com', '학생홍', NULL, '01009876543', 'MEMBER'),
('student21', 'pw21', '고학생', 'student21@example.com', '학생고', NULL, '01055551111', 'MEMBER'),
('student22', 'pw22', '문학생', 'student22@example.com', '학생문', NULL, '01055552222', 'MEMBER'),
('student23', 'pw23', '양학생', 'student23@example.com', '학생양', NULL, '01055553333', 'MEMBER'),
('student24', 'pw24', '손학생', 'student24@example.com', '학생손', NULL, '01055554444', 'MEMBER'),
('student25', 'pw25', '배학생', 'student25@example.com', '학생배', NULL, '01055555555', 'MEMBER'),
('student26', 'pw26', '조학생', 'student26@example.com', '학생조2', NULL, '01055556666', 'MEMBER'),
('student27', 'pw27', '백학생', 'student27@example.com', '학생백', NULL, '01055557777', 'MEMBER'),
('student28', 'pw28', '허학생', 'student28@example.com', '학생허', NULL, '01055558888', 'MEMBER'),
('student29', 'pw29', '노학생', 'student29@example.com', '학생노', NULL, '01055559999', 'MEMBER'),
('student30', 'pw30', '남학생', 'student30@example.com', '학생남', NULL, '01055550000', 'MEMBER'),
('coach1', 'pw1', '김코치', 'kim1@example.com', '코치킴', NULL, '01011112222', 'INSTRUCTOR'),
('coach2', 'pw2', '이코치', 'lee2@example.com', '코치이', NULL, '01022223333', 'INSTRUCTOR'),
('coach3', 'pw3', '박코치', 'park3@example.com', '코치박', NULL, '01033334444', 'INSTRUCTOR'),
('coach4', 'pw4', '최코치', 'choi4@example.com', '코치최', NULL, '01044445555', 'INSTRUCTOR'),
('coach5', 'pw5', '정코치', 'jung5@example.com', '코치정', NULL, '01055556666', 'INSTRUCTOR'),
('coach6', 'pw6', '윤코치', 'yoon6@example.com', '코치윤', NULL, '01066667777', 'INSTRUCTOR'),
('coach7', 'pw7', '장코치', 'jang7@example.com', '코치장', NULL, '01077778888', 'INSTRUCTOR'),
('coach8', 'pw8', '한코치', 'han8@example.com', '코치한', NULL, '01088889999', 'INSTRUCTOR'),
('coach9', 'pw9', '서코치', 'seo9@example.com', '코치서', NULL, '01099990000', 'INSTRUCTOR'),
('coach10', 'pw10', '노코치', 'noh10@example.com', '코치노', NULL, '01010101010', 'INSTRUCTOR'),
('coach11', 'pw11', '배코치', 'bae11@example.com', '코치배', NULL, '01011113333', 'INSTRUCTOR'),
('coach12', 'pw12', '문코치', 'moon12@example.com', '코치문', NULL, '01022224444', 'INSTRUCTOR'),
('coach13', 'pw13', '안코치', 'ahn13@example.com', '코치안', NULL, '01033335555', 'INSTRUCTOR'),
('coach14', 'pw14', '황코치', 'hwang14@example.com', '코치황', NULL, '01044446666', 'INSTRUCTOR'),
('coach15', 'pw15', '류코치', 'ryu15@example.com', '코치류', NULL, '01055557777', 'INSTRUCTOR'),
('coach16', 'pw16', '조코치', 'jo16@example.com', '코치조', NULL, '01066668888', 'INSTRUCTOR'),
('coach17', 'pw17', '백코치', 'baek17@example.com', '코치백', NULL, '01077779999', 'INSTRUCTOR'),
('coach18', 'pw18', '강코치', 'kang18@example.com', '코치강', NULL, '01088880000', 'INSTRUCTOR'),
('coach19', 'pw19', '정코치', 'jung19@example.com', '코치정2', NULL, '01099991111', 'INSTRUCTOR'),
('coach20', 'pw20', '이코치', 'lee20@example.com', '코치이2', NULL, '01010102222', 'INSTRUCTOR');

-- Game 데이터 삽입 (예시 - 일부 학생들에게만 게임 정보 추가)
INSERT INTO game (member_id, game_type, game_tier, game_position)
VALUES
(1, 'LOL', 'BRONZE', 'TOP'),
(2, 'LOL', 'SILVER', 'JUNGLE'),
(3, 'LOL', 'GOLD', 'MID'),
(4, 'LOL', 'PLATINUM', 'ADC'),
(5, 'LOL', 'DIAMOND', 'SUPPORT'),
(6, 'VALORANT', 'BRONZE', 'DUELIST'),
(7, 'VALORANT', 'SILVER', 'SENTINEL'),
(8, 'VALORANT', 'GOLD', 'INITIATOR'),
(9, 'VALORANT', 'PLATINUM', 'CONTROLLER'),
(10, 'PUBG', 'AMATEUR', 'RUSHER'),
(11, 'PUBG', 'INTERMEDIATE', 'SNIPER'),
(12, 'OVERWATCH', 'BRONZE', 'TANK'),
(13, 'OVERWATCH', 'SILVER', 'DAMAGE'),
(14, 'OVERWATCH', 'GOLD', 'SUPPORT'),
(15, 'STARCRAFT', 'BRONZE', 'TERRAN');

-- Instructor 데이터 삽입 (강사 정보)
INSERT INTO instructor (member_id, career, description, review_num, average_rating, created_at, total_members)
VALUES
(31, '라인전 특화 2년', '탑 라인 기본기 훈련', 12, 4.8, NOW(), 25),
(32, '정글 운영 전문가', '숲속의 전략가', 15, 4.7, NOW(), 30),
(33, '미드라인 심리전 마스터', '타이밍 계산법 전수', 10, 4.9, NOW(), 20),
(34, 'LOL 분석 코치', '롤 전반적 이해력 향상', 8, 4.5, NOW(), 15),
(35, '입문 특화 코치', '초보자 친절 코칭', 20, 4.6, NOW(), 40),
(36, '발로란트 에이밍 전문', '정확한 조준과 판단력', 18, 4.8, NOW(), 35),
(37, 'TFT 고랭커', '배치 운용 집중 교육', 6, 4.3, NOW(), 12),
(38, 'LOL 팀전 전문', '소통과 협동 중심 수업', 11, 4.7, NOW(), 22),
(39, '프로 준비생 멘토', '심화 전략과 목표 설정', 9, 4.9, NOW(), 18),
(40, '밸런스형 강의자', '고루 갖춘 피드백 제공', 14, 4.6, NOW(), 28),
(41, '라인전 타워 다이브 전문가', '탑 라인에서의 타워 다이브 집중 코칭', 8, 4.6, NOW(), 16),
(42, '정글 피지컬 훈련 전문', '정글러의 1:1 싸움 능력 극대화', 12, 4.7, NOW(), 24),
(43, '미드 포킹 마스터', '미드에서 포킹 챔피언 운영법 전수', 7, 4.5, NOW(), 14),
(44, '초보자 눈높이 교사', 'LOL 완전 입문자 대상 강의', 20, 4.4, NOW(), 40),
(45, 'VALORANT 수비 전술가', '수비 시 포지션별 유틸 활용 교육', 10, 4.8, NOW(), 20),
(46, 'VALORANT 옵저버 역할 훈련', '오더/인게임 리더 특화', 6, 4.6, NOW(), 12),
(47, 'TFT 오픈 포트 전략 마스터', '고수들이 쓰는 오픈 전략 분석', 9, 4.7, NOW(), 18),
(48, 'TFT 초보자용 천천히 배워요 코치', '배치 탈출을 위한 완전 기초 강의', 15, 4.5, NOW(), 30),
(49, 'LOL 입문 강의 재정의', '게임 용어, 클라이언트, 챔피언 이해', 25, 4.6, NOW(), 50),
(50, 'LOL 팀 랭크 전략가', '소규모 팀 단위 전략과 운영 중심 강의', 11, 4.8, NOW(), 22);
