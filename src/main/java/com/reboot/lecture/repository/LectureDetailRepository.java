package com.reboot.lecture.repository;

import com.reboot.lecture.entity.LectureDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LectureDetailRepository extends JpaRepository<LectureDetail, Long> {

    // 강의 ID로 LectureDetail 조회
    @Query("SELECT ld FROM LectureDetail ld JOIN FETCH ld.lecture l WHERE l.id = :lectureId")
    Optional<LectureDetail> findByLectureId(@Param("lectureId") Long lectureId);
}