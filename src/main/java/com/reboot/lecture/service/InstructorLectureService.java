package com.reboot.lecture.service;

import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;

import java.util.List;


// 강사 전용 강의 관리 서비스 인터페이스
// 강사의 자신의 강의에 대한 CRUD 작업을 처리
public interface InstructorLectureService {


    // 특정 강사의 모든 강의 목록 조회
    List<LectureResponseDto> getLecturesByInstructor(Long instructorId);


    // 특정 강의 상세 정보 조회 (특정 강사의 강의만)
    LectureResponseDto getLectureByIdAndInstructor(String lectureId, Long instructorId);


    // 새 강의 생성
    LectureResponseDto createLecture(LectureRequestDto request, Instructor instructor);


    // 기존 강의 수정 (특정 강사의 강의만)
    LectureResponseDto updateLecture(String lectureId, LectureRequestDto request, Long instructorId);


    // 강의 삭제 (특정 강사의 강의만)
    void deleteLecture(String lectureId, Long instructorId);


    // 강의 활성화/비활성화 토글 (특정 강사의 강의만)
    // 실제 구현에서는 기능을 변경하거나 제거할 수 있음
    LectureResponseDto toggleLectureActive(String lectureId, Long instructorId);



    // 추가: 강사의 게임 정보 관련 메서드
    Game getInstructorGame(Long instructorId);
    String getInstructorGameType(Long instructorId);
    String getInstructorGamePosition(Long instructorId);
    String getInstructorGameTier(Long instructorId);
}