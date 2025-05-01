package com.reboot.lecture.repository;

import com.reboot.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, String> {
    // 기본 CRUD 메소드는 JpaRepository에서 제공
    // 추가 쿼리 메소드가 필요하면 여기에 정의
}
