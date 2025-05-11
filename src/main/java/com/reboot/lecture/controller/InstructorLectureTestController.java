package com.reboot.lecture.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.service.InstructorLectureService;
import com.reboot.lecture.service.LectureFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 강의 생성, 수정, 삭제, 활성/비활성화 테스트 (Swagger)
@RestController
@RequestMapping("/api/test/instructor/lectures")
@RequiredArgsConstructor
@Tag(name = "강사 - 강의 관리 테스트 API", description = "강사 본인 강의 CRUD 테스트용 API")
public class InstructorLectureTestController {

    private final InstructorLectureService instructorLectureService;
    private final LectureFileService lectureFileService; // 파일 서비스 추가

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

    // 새 강의 생성 (파일 업로드 지원)
    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "테스트: 새 강의 생성 (파일 업로드)", description = "지정한 강사로 새 강의 생성 (파일 업로드 지원)")
    public ResponseEntity<LectureResponseDto> createLecture(
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId,
            @RequestPart("request") LectureRequestDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "preview", required = false) MultipartFile preview,
            @RequestPart(value = "materials", required = false) List<MultipartFile> materials) {

        try {
            // 폴더 경로 설정
            String folder = "lectures/instructor_" + instructorId;

            // 1. 썸네일 업로드
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = lectureFileService.uploadThumbnail(thumbnail, folder + "/thumbnails");
                request.setImageUrl(thumbnailUrl);
            }

            // 2. 비디오 업로드
            if (video != null && !video.isEmpty()) {
                String videoUrl = lectureFileService.uploadVideo(video, folder + "/videos");
                request.setVideoUrl(videoUrl);
            }

            // 3. 미리보기 영상 업로드
            if (preview != null && !preview.isEmpty()) {
                String previewUrl = lectureFileService.uploadVideo(preview, folder + "/previews");
                request.setPreviewUrl(previewUrl);
            }

            // 4. 강의 자료 업로드 (여러 파일)
            if (materials != null && !materials.isEmpty()) {
                List<String> materialUrls = new ArrayList<>();
                for (MultipartFile material : materials) {
                    if (!material.isEmpty()) {
                        String materialUrl = lectureFileService.uploadMaterial(material, folder + "/materials");
                        materialUrls.add(materialUrl);
                    }
                }
                request.setMaterialUrls(String.join(",", materialUrls));
            }

            // 5. 강의 생성
            Instructor instructor = new Instructor();
            instructor.setInstructorId(instructorId);
            LectureResponseDto createdLecture = instructorLectureService.createLecture(request, instructor);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdLecture);

        } catch (IOException e) {
            e.printStackTrace(); // 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 기존 강의 수정 - 파일 업로드 지원
    @PutMapping(value = "/{lectureId}", consumes = {"multipart/form-data"})
    @Operation(summary = "테스트: 강의 수정 (파일 업로드)", description = "강사 ID와 강의 ID로 강의 정보 수정 (파일 업로드 지원)")
    public ResponseEntity<LectureResponseDto> updateLecture(
            @Parameter(description = "강의 ID (형식: LECTURE-숫자 또는 숫자)")
            @PathVariable String lectureId,
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId,
            @RequestPart("request") LectureRequestDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "preview", required = false) MultipartFile preview,
            @RequestPart(value = "materials", required = false) List<MultipartFile> materials) {

        try {
            // 폴더 경로 설정
            String folder = "lectures/instructor_" + instructorId;

            // 1. 썸네일 업로드 (있는 경우)
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = lectureFileService.uploadThumbnail(thumbnail, folder + "/thumbnails");
                request.setImageUrl(thumbnailUrl);
            }

            // 2. 비디오 업로드 (있는 경우)
            if (video != null && !video.isEmpty()) {
                String videoUrl = lectureFileService.uploadVideo(video, folder + "/videos");
                request.setVideoUrl(videoUrl);
            }

            // 3. 미리보기 영상 업로드 (있는 경우)
            if (preview != null && !preview.isEmpty()) {
                String previewUrl = lectureFileService.uploadVideo(preview, folder + "/previews");
                request.setPreviewUrl(previewUrl);
            }

            // 4. 강의 자료 업로드 (여러 파일, 있는 경우)
            if (materials != null && !materials.isEmpty()) {
                List<String> materialUrls = new ArrayList<>();
                for (MultipartFile material : materials) {
                    if (!material.isEmpty()) {
                        String materialUrl = lectureFileService.uploadMaterial(material, folder + "/materials");
                        materialUrls.add(materialUrl);
                    }
                }
                request.setMaterialUrls(String.join(",", materialUrls));
            }

            // 5. 강의 수정
            LectureResponseDto updatedLecture = instructorLectureService.updateLecture(
                    lectureId, request, instructorId
            );
            return ResponseEntity.ok(updatedLecture);

        } catch (IOException e) {
            e.printStackTrace(); // 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 원래 있던 JSON 기반 강의 생성 메서드 - 메서드명 변경
    @PostMapping("/json")
    @Operation(summary = "테스트: 새 강의 생성 (JSON)", description = "지정한 강사로 새 강의 생성 (파일 업로드 없음)")
    public LectureResponseDto createLectureJson(
            @Parameter(description = "강사 ID")
            @RequestParam Long instructorId,
            @RequestBody LectureRequestDto request) {

        // Instructor 객체를 직접 생성해서 전달
        Instructor instructor = new Instructor();
        instructor.setInstructorId(instructorId);
        return instructorLectureService.createLecture(request, instructor);
    }

    // 원래 있던 JSON 기반 강의 수정 메서드 - 메서드명 변경
    @PutMapping("/{lectureId}/json")
    @Operation(summary = "테스트: 강의 수정 (JSON)", description = "강사 ID와 강의 ID로 강의 정보 수정 (파일 업로드 없음)")
    public LectureResponseDto updateLectureJson(
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

    // 파일 업로드가 성공적으로 완료되었는지 확인하는 테스트 메서드 추가
    @GetMapping("/file-check")
    @Operation(summary = "테스트: 파일 존재 확인", description = "특정 파일이 스토리지에 업로드되었는지 확인")
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @Parameter(description = "파일 URL")
            @RequestParam String fileUrl) {

        boolean exists = lectureFileService.checkFileExists(fileUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("fileExists", exists);
        response.put("url", fileUrl);
        response.put("message", exists ?
                "파일이 스토리지에 존재합니다." :
                "파일이 스토리지에 존재하지 않습니다.");

        return ResponseEntity.ok(response);
    }

    // 파일 상세 정보 조회 메서드 추가
    @GetMapping("/file-info")
    @Operation(summary = "테스트: 파일 정보 조회", description = "특정 파일의 메타데이터 정보 조회")
    public ResponseEntity<String> getFileInfo(
            @Parameter(description = "파일 URL")
            @RequestParam String fileUrl) {

        String fileInfo = lectureFileService.getFileInfo(fileUrl);
        return ResponseEntity.ok(fileInfo);
    }
}