INSERT INTO member (member_id, username, password, name, nickname, email, profile_image, phone, role, custom_service)
VALUES (1, 'user1', 'pw', '홍길동', 'hong', 'hong@test.com', NULL, '010-0000-0000', 'USER', NULL);

INSERT INTO instructor (instructor_id, member_id, career, description, review_num, rating)
VALUES (1, 1, '5년차', '설명 잘함', 0, 0.0);

INSERT INTO lecture (lecture_id, instructor_id, title, description, game_type, price)
VALUES (1, 1, '롤 강의', '실전 위주 강의입니다', 'LOL', 30000);