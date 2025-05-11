package com.reboot.lecture.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.InstructorAuthService;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.service.InstructorLectureService;
import com.reboot.lecture.service.LectureFileService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Hidden // Swagger에서 숨김
// 강사 전용 강의 관리 컨트롤러
// 강사가 자신의 강의를 생성, 조회, 수정, 삭제
@Tag(name = "강사 - 강의 관리 API", description = "강사가 자신의 강의를 생성, 조회, 수정, 삭제하는 API")
@Controller
@RequestMapping("/instructor/lectures")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')") // 강사 권한이 있는 사용자만 접근 가능
public class InstructorLectureController {

    private final InstructorLectureService instructorLectureService;
    private final InstructorAuthService instructorAuthService;
    private final LectureFileService lectureFileService;

    // 현재 로그인한 강사의 모든 강의 목록 조회
    @GetMapping
    @Operation(summary = "강사 강의 목록 조회", description = "지정한 강사의 모든 강의 조회")
    public ResponseEntity<List<LectureResponseDto>> getMyLectures() {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        List<LectureResponseDto> lectures = instructorLectureService.getLecturesByInstructor(instructor.getInstructorId());
        return ResponseEntity.ok(lectures);
    }

    // 강의 상세 정보 조회 (본인 강의만)
    @GetMapping("/{lectureId}")
    @Operation(summary = "강의 상세 조회", description = "강사 ID와 강의 ID로 강의 상세 정보 조회")
    public ResponseEntity<LectureResponseDto> getLecture(@PathVariable String lectureId) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        LectureResponseDto lecture = instructorLectureService.getLectureByIdAndInstructor(
                lectureId, instructor.getInstructorId());
        return ResponseEntity.ok(lecture);
    }

    // 새 강의 생성 (파일 업로드 지원)
    @PostMapping
    @Operation(summary = "새 강의 생성", description = "지정한 강사로 새 강의 생성")
    public ResponseEntity<LectureResponseDto> createLecture(
            @RequestPart("request") LectureRequestDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "preview", required = false) MultipartFile preview,
            @RequestPart(value = "materials", required = false) List<MultipartFile> materials) {

        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();
            String instructorId = instructor.getInstructorId().toString();
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
            LectureResponseDto createdLecture = instructorLectureService.createLecture(request, instructor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLecture);

        } catch (IOException e) {
            e.printStackTrace(); // 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 기존 강의 수정 (본인 강의만) - 파일 업로드 지원
    @PutMapping("/{lectureId}")
    @Operation(summary = "강의 수정", description = "강사 ID와 강의 ID로 강의 정보 수정")
    public ResponseEntity<LectureResponseDto> updateLecture(
            @PathVariable String lectureId,
            @RequestPart("request") LectureRequestDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "preview", required = false) MultipartFile preview,
            @RequestPart(value = "materials", required = false) List<MultipartFile> materials) {

        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();
            String instructorId = instructor.getInstructorId().toString();
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
                    lectureId, request, instructor.getInstructorId()
            );
            return ResponseEntity.ok(updatedLecture);

        } catch (IOException e) {
            e.printStackTrace(); // 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 강의 삭제 (본인 강의만)
    @DeleteMapping("/{lectureId}")
    @Operation(summary = "테스트: 강의 삭제", description = "강의 삭제 (본인 강의만)")
    public ResponseEntity<Void> deleteLecture(@PathVariable String lectureId) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        instructorLectureService.deleteLecture(lectureId, instructor.getInstructorId());
        return ResponseEntity.noContent().build();
    }

    // 강의 활성화/비활성화 토글 (본인 강의만)
    @PatchMapping("/{lectureId}/toggle-active")
    @Operation(summary = "활성화/비활성화 토글", description = "강의 활성화 상태 토글 (본인 강의만)")
    public ResponseEntity<LectureResponseDto> toggleLectureActive(@PathVariable String lectureId) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        LectureResponseDto lecture = instructorLectureService.toggleLectureActive(
                lectureId, instructor.getInstructorId());
        return ResponseEntity.ok(lecture);
    }
}