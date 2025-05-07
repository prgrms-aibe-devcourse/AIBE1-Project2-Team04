package com.reboot.lecture.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.dto.LectureRequest;
import com.reboot.lecture.dto.LectureResponse;

import java.util.List;


// 강사 전용 강의 관리 서비스 인터페이스
// 강사의 자신의 강의에 대한 CRUD 작업을 처리
public interface InstructorLectureService {


    // 특정 강사의 모든 강의 목록 조회
    List<LectureResponse> getLecturesByInstructor(Long instructorId);


    // 특정 강의 상세 정보 조회 (특정 강사의 강의만)
    LectureResponse getLectureByIdAndInstructor(String lectureId, Long instructorId);


    // 새 강의 생성
    LectureResponse createLecture(LectureRequest request, Instructor instructor);


    // 기존 강의 수정 (특정 강사의 강의만)
    LectureResponse updateLecture(String lectureId, LectureRequest request, Long instructorId);


    // 강의 삭제 (특정 강사의 강의만)
    // 물리적 삭제가 아닌 소프트 삭제 처리
    void deleteLecture(String lectureId, Long instructorId);


    // 강의 활성화/비활성화 토글 (특정 강사의 강의만)
    LectureResponse toggleLectureActive(String lectureId, Long instructorId);
}