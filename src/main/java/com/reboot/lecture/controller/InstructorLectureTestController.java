package com.reboot.lecture.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.dto.LectureRequest;
import com.reboot.lecture.dto.LectureResponse;
import com.reboot.lecture.service.InstructorLectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 강의 생성, 수정, 삭제, 활성/비활성화 테스트 (Swagger)
@RestController
@RequestMapping("/api/instructor/test/lectures")
@RequiredArgsConstructor
@Tag(name = "강사 강의 관리 테스트 API", description = "강사 본인 강의 CRUD 테스트용 API")
public class InstructorLectureTestController {

    private final InstructorLectureService instructorLectureService;

    @GetMapping
    @Operation(summary = "테스트: 강사 강의 목록 조회", description = "지정한 강사의 모든 강의 조회")
    public List<LectureResponse> getMyLectures(@RequestParam Long instructorId) {
        return instructorLectureService.getLecturesByInstructor(instructorId);
    }

    @GetMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 상세 조회", description = "강사 ID와 강의 ID로 강의 상세 정보 조회")
    public LectureResponse getLecture(
            @PathVariable String lectureId,
            @RequestParam Long instructorId) {
        return instructorLectureService.getLectureByIdAndInstructor(lectureId, instructorId);
    }

    @PostMapping
    @Operation(summary = "테스트: 새 강의 생성", description = "지정한 강사로 새 강의 생성")
    public LectureResponse createLecture(
            @RequestParam Long instructorId,
            @RequestBody LectureRequest request) {
        // Instructor 객체를 직접 생성해서 전달
        Instructor instructor = new Instructor();
        instructor.setInstructorId(instructorId);
        return instructorLectureService.createLecture(request, instructor);
    }

    @PutMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 수정", description = "강사 ID와 강의 ID로 강의 정보 수정")
    public LectureResponse updateLecture(
            @PathVariable String lectureId,
            @RequestParam Long instructorId,
            @RequestBody LectureRequest request) {
        return instructorLectureService.updateLecture(lectureId, request, instructorId);
    }

    @DeleteMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 삭제", description = "강의 소프트 삭제 (본인 강의만)")
    public void deleteLecture(
            @PathVariable String lectureId,
            @RequestParam Long instructorId) {
        instructorLectureService.deleteLecture(lectureId, instructorId);
    }

    @PatchMapping("/{lectureId}/toggle-active")
    @Operation(summary = "테스트: 활성화/비활성화 토글", description = "강의 활성화 상태 토글 (본인 강의만)")
    public LectureResponse toggleLectureActive(
            @PathVariable String lectureId,
            @RequestParam Long instructorId) {
        return instructorLectureService.toggleLectureActive(lectureId, instructorId);
    }
}
