package com.reboot.lecture.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.service.InstructorLectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 강의 생성, 수정, 삭제, 활성/비활성화 테스트 (Swagger)
@RestController
@RequestMapping("/api/test/instructor/lectures")
@RequiredArgsConstructor
@Tag(name = "강사 - 강의 관리 테스트 API", description = "강사 본인 강의 CRUD 테스트용 API")
public class InstructorLectureTestController {

    private final InstructorLectureService instructorLectureService;

    @GetMapping
    @Operation(summary = "테스트: 강사 강의 목록 조회", description = "지정한 강사의 모든 강의 조회")
    public List<LectureResponseDto> getMyLectures(
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId) {

        return instructorLectureService.getLecturesByInstructor(instructorId);
    }

    @GetMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 상세 조회", description = "강사 ID와 강의 ID로 강의 상세 정보 조회")
    public LectureResponseDto getLecture(
            @Parameter(description = "강의 ID (형식: LECTURE-숫자 또는 숫자)")
            @PathVariable String lectureId,
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId) {

        return instructorLectureService.getLectureByIdAndInstructor(lectureId, instructorId);
    }

    @PostMapping
    @Operation(summary = "테스트: 새 강의 생성", description = "지정한 강사로 새 강의 생성")
    public LectureResponseDto createLecture(
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId,
            @RequestBody LectureRequestDto request) {

        // Instructor 객체를 직접 생성해서 전달
        Instructor instructor = new Instructor();
        instructor.setInstructorId(instructorId);
        return instructorLectureService.createLecture(request, instructor);
    }

    @PutMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 수정", description = "강사 ID와 강의 ID로 강의 정보 수정")
    public LectureResponseDto updateLecture(
            @Parameter(description = "강의 ID (형식: LECTURE-숫자 또는 숫자)")
            @PathVariable String lectureId,
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId,
            @RequestBody LectureRequestDto request) {

        return instructorLectureService.updateLecture(lectureId, request, instructorId);
    }

    @DeleteMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 삭제", description = "강의 삭제 (본인 강의만)")
    public void deleteLecture(
            @Parameter(description = "강의 ID (형식: LECTURE-숫자 또는 숫자)")
            @PathVariable String lectureId,
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId) {

        instructorLectureService.deleteLecture(lectureId, instructorId);
    }

    @PatchMapping("/{lectureId}/toggle-active")
    @Operation(summary = "테스트: 강의 상태 토글", description = "강의 활성화/비활성화 상태 토글 (본인 강의만)")
    public LectureResponseDto toggleLectureActive(
            @Parameter(description = "강의 ID (형식: LECTURE-숫자 또는 숫자)")
            @PathVariable String lectureId,
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId) {

        return instructorLectureService.toggleLectureActive(lectureId, instructorId);
    }

    // 새 메서드 추가: 강사 게임 정보 가져오기
    @GetMapping("/{instructorId}/game-info")
    @Operation(summary = "테스트: 강사 게임 정보 조회", description = "강사의 게임 정보(타입, 포지션, 티어) 조회")
    public Object getInstructorGameInfo(
            @Parameter(description = "강사 ID")
            @PathVariable Long instructorId) {

        // 간단한 응답 객체를 만들어 반환
        return new Object() {
            public final String gameType = instructorLectureService.getInstructorGameType(instructorId);
            public final String gamePosition = instructorLectureService.getInstructorGamePosition(instructorId);
            public final String gameTier = instructorLectureService.getInstructorGameTier(instructorId);
        };
    }
}