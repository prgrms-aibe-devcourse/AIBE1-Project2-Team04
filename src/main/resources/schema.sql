-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS replay;
DROP TABLE IF EXISTS refund_history;
DROP TABLE IF EXISTS toss_transaction;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS survey;
DROP TABLE IF EXISTS lecture_ibfk_1;
DROP TABLE IF EXISTS lecture;
DROP TABLE IF EXISTS instructor;
DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS member;

-- Create Member table
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

-- Create Game table with one-to-one relationship to Member
CREATE TABLE game (
    game_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,  -- Added UNIQUE for one-to-one relationship
    game_type VARCHAR(50),
    game_tier VARCHAR(50),
    game_position VARCHAR(50),
    FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB;

-- Create Instructor table with one-to-one relationship to Member
CREATE TABLE instructor (
    instructor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,  -- Added UNIQUE for one-to-one relationship
    career VARCHAR(1000),
    description VARCHAR(2000),
    review_num INT DEFAULT 0,
    average_rating DOUBLE DEFAULT 0.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_members INT DEFAULT 0,
    FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB;

-- Create Lecture table with embedded LectureInfo and LectureMetaData
CREATE TABLE lecture (
    lecture_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instructor_id BIGINT NOT NULL,

    -- LectureInfo fields
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    game_type VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    duration INTEGER NOT NULL,
    lecture_rank VARCHAR(255) NOT NULL,  -- Changed from game_tier to lecture_rank per entity
    position VARCHAR(255) NOT NULL,      -- Changed from game_position to position per entity

    -- LectureMetaData fields
    average_rating FLOAT DEFAULT 0.0 NOT NULL,
    total_members INT DEFAULT 0 NOT NULL,
    review_count INT DEFAULT 0 NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id)
) ENGINE=InnoDB;

-- Create Survey table
CREATE TABLE survey (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    game_type VARCHAR(50),
    game_tier VARCHAR(255),
    game_position VARCHAR(255),
    skill_level VARCHAR(50),
    learning_goal VARCHAR(50),
    available_time VARCHAR(50),
    lecture_preference VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB;

-- Create Reservation table based on Reservation entity
CREATE TABLE reservation (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    lecture_id BIGINT NOT NULL,
    request_detail TEXT,
    schedule_date VARCHAR(20),  -- yyyy-MM-dd format as per entity
    date DATETIME,
    status VARCHAR(50),
    cancel_reason VARCHAR(500),

    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id),
    FOREIGN KEY (lecture_id) REFERENCES lecture(lecture_id)
) ENGINE=InnoDB;

-- Create Payment table
CREATE TABLE payment (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT UNIQUE NOT NULL,
    amount DECIMAL(10, 2),
    payment_date DATETIME,
    payment_status VARCHAR(50),
    payment_method VARCHAR(50),

    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) ENGINE=InnoDB;

-- Create Replay table
CREATE TABLE replay (
    replay_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    file_url VARCHAR(500),
    date DATETIME,

    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) ENGINE=InnoDB;