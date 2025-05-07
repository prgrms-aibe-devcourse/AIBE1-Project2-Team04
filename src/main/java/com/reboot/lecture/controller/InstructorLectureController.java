package com.reboot.lecture.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.AuthService;
import com.reboot.lecture.dto.LectureRequest;
import com.reboot.lecture.dto.LectureResponse;
import com.reboot.lecture.service.InstructorLectureService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 강사 전용 강의 관리 컨트롤러
// 강사가 자신의 강의를 생성, 조회, 수정, 삭제
@Tag(name = "강사 강의 관리 API", description = "강사가 자신의 강의를 생성, 조회, 수정, 삭제하는 API")
@RestController
@RequestMapping("/api/instructor/lectures")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')") // 강사 권한이 있는 사용자만 접근 가능
public class InstructorLectureController {

    private final InstructorLectureService instructorLectureService;
    private final AuthService authService;


    // 현재 로그인한 강사의 모든 강의 목록 조회
    @GetMapping
    public ResponseEntity<List<LectureResponse>> getMyLectures() {
        Instructor instructor = authService.getCurrentInstructor();
        List<LectureResponse> lectures = instructorLectureService.getLecturesByInstructor(instructor.getInstructorId());
        return ResponseEntity.ok(lectures);
    }


    // 강의 상세 정보 조회 (본인 강의만)
    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureResponse> getLecture(@PathVariable String lectureId) {
        Instructor instructor = authService.getCurrentInstructor();
        LectureResponse lecture = instructorLectureService.getLectureByIdAndInstructor(
                lectureId, instructor.getInstructorId());
        return ResponseEntity.ok(lecture);
    }


    // 새 강의 생성
    @PostMapping
    public ResponseEntity<LectureResponse> createLecture(@RequestBody LectureRequest request) {
        Instructor instructor = authService.getCurrentInstructor();
        LectureResponse createdLecture = instructorLectureService.createLecture(request, instructor);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLecture);
    }


    // 기존 강의 수정 (본인 강의만)
    @PutMapping("/{lectureId}")
    public ResponseEntity<LectureResponse> updateLecture(
            @PathVariable String lectureId,
            @RequestBody LectureRequest request) {

        Instructor instructor = authService.getCurrentInstructor();
        LectureResponse updatedLecture = instructorLectureService.updateLecture(
                lectureId, request, instructor.getInstructorId());
        return ResponseEntity.ok(updatedLecture);
    }


    // 강의 삭제 (본인 강의만) - 소프트 삭제 처리
    @DeleteMapping("/{lectureId}")
    public ResponseEntity<Void> deleteLecture(@PathVariable String lectureId) {
        Instructor instructor = authService.getCurrentInstructor();
        instructorLectureService.deleteLecture(lectureId, instructor.getInstructorId());
        return ResponseEntity.noContent().build();
    }


    // 강의 활성화/비활성화 토글 (본인 강의만)
    @PatchMapping("/{lectureId}/toggle-active")
    public ResponseEntity<LectureResponse> toggleLectureActive(@PathVariable String lectureId) {
        Instructor instructor = authService.getCurrentInstructor();
        LectureResponse lecture = instructorLectureService.toggleLectureActive(
                lectureId, instructor.getInstructorId());
        return ResponseEntity.ok(lecture);
    }
}