-- Member
CREATE TABLE member (
                        member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        nickname VARCHAR(255) NOT NULL UNIQUE,
                        profile_image VARCHAR(255),
                        phone VARCHAR(50),
                        role VARCHAR(50)
);

-- Game
CREATE TABLE game (
                      game_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      member_id BIGINT NOT NULL UNIQUE,
                      game_type VARCHAR(255),
                      game_tier VARCHAR(100),
                      game_position VARCHAR(100),
                      CONSTRAINT fk_game_member
                          FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- Instructor
CREATE TABLE instructor (
                            instructor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            member_id BIGINT NOT NULL UNIQUE,
                            career VARCHAR(255),
                            description VARCHAR(500),
                            review_num INT,
                            rating DOUBLE,
                            CONSTRAINT fk_instructor_member
                                FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- Lecture
CREATE TABLE lecture (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         instructor_id BIGINT NOT NULL,
                         info_title VARCHAR(255),
                         info_description VARCHAR(500),
                         info_game_type VARCHAR(100),
                         info_price DECIMAL(15,2),
                         info_image_url VARCHAR(255),
                         info_duration INT,
                         info_rank_ VARCHAR(50),
                         info_position VARCHAR(50),
                         metadata_average_rating FLOAT,
                         metadata_total_members INT,
                         metadata_review_count INT,
                         metadata_created_at DATETIME,
                         metadata_updated_at DATETIME,
                         metadata_deleted_at DATETIME,
                         metadata_is_active BOOLEAN,
                         CONSTRAINT fk_lecture_instructor
                             FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id)
);

-- Reservation
CREATE TABLE reservation (
                             reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             instructor_id BIGINT NOT NULL,
                             lecture_id BIGINT NOT NULL,
                             date DATETIME,
                             status VARCHAR(50),
                             request_detail VARCHAR(500),
                             youtube_url VARCHAR(255),
                             schedule_date DATETIME,
                             CONSTRAINT fk_reservation_member
                                 FOREIGN KEY (member_id) REFERENCES member(member_id),
                             CONSTRAINT fk_reservation_instructor
                                 FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id),
                             CONSTRAINT fk_reservation_lecture
                                 FOREIGN KEY (lecture_id) REFERENCES lecture(id)
);

-- Payment
CREATE TABLE payment (
                         payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         reservation_id BIGINT NOT NULL,
                         price integer,
                         payment_at DATETIME,
                         method VARCHAR(50),
                         status VARCHAR(50),
                         CONSTRAINT fk_payment_reservation
                             FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
);


-- MEMBER (학생/강사 공통)
INSERT INTO member (member_id, username, password, name, email, nickname, profile_image, phone, role)
VALUES
    (1, 'tester1', 'pw1234', '홍길동', 'hong1@test.com', '길동1', NULL, '010-1234-5678', 'STUDENT'),
    (2, 'instructor1', 'pw5678', '이몽룡', 'mong1@test.com', '몽룡1', NULL, '010-8765-4321', 'INSTRUCTOR');

-- GAME (학생 회원 1명에 대한 게임 정보)
INSERT INTO game (game_id, member_id, game_type, game_tier, game_position)
VALUES
    (1, 1, 'LOL', '플래티넘', '탑');

-- INSTRUCTOR (강사 정보)
INSERT INTO instructor (instructor_id, member_id, career, description, review_num, rating)
VALUES
    (1, 2, 'Java Instructor', 'Experienced Java instructor', 0, 4.5);

-- LECTURE (강사의 강의 정보, metadata는 임시값)
INSERT INTO lecture (id, instructor_id, info_title, info_description, info_game_type, info_price,info_image_url,info_duration,info_rank_,info_position,metadata_average_rating,metadata_total_members,metadata_review_count,metadata_created_at,metadata_updated_at,metadata_deleted_at,metadata_is_active)
VALUES (1, 1, 'LOL 정글 강의', '정글 클리어와 갱킹 타이밍 수업', 'LOL', 50000,
        'https://example.com/jungle.jpg', 60, '다이아몬드', '정글',
        4.7, 23, 7, NOW(), NOW(), NULL, true);

-- RESERVATION (수업 예약)
INSERT INTO reservation (reservation_id, member_id, instructor_id, lecture_id, date, status, request_detail, youtube_url, schedule_date)
VALUES
    (1, 1, 1, 1, NOW(), 'RESERVED', '자바 기초를 배우고 싶어요', NULL, '2024-07-01 20:00:00');

-- PAYMENT (결제)
INSERT INTO payment (payment_id, reservation_id, price, payment_at, method, status)
VALUES
    (1, 1, 35000, NOW(), 'CARD', 'READY');