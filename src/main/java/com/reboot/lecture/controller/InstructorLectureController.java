package com.reboot.lecture.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.InstructorAuthService;
import com.reboot.auth.service.FileUploadService;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.dto.LectureDetailResponseDto;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.service.InstructorLectureService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Hidden // Swagger에서 숨김
@Tag(name = "강사 - 강의 관리 API", description = "강사가 자신의 강의를 생성, 조회, 수정, 삭제하는 API")
@Controller
@RequestMapping("/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')") // 강사 권한이 있는 사용자만 접근 가능
public class InstructorLectureController {

    private final InstructorLectureService instructorLectureService;
    private final InstructorAuthService instructorAuthService;
    private final FileUploadService fileUploadService;

    // === 뷰를 반환하는 메서드들 ===

    // 강사 마이페이지 (강의 목록 포함)
    @GetMapping("/mypage")
    public String mypage(Model model) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        List<LectureResponseDto> lectures = instructorLectureService.getLecturesByInstructor(instructor.getInstructorId());

        model.addAttribute("instructor", instructor);
        model.addAttribute("member", instructor.getMember());  // member 추가
        model.addAttribute("lectures", lectures);
        return "instructor/mypage/index";
    }

    // 강의 목록 페이지 (리다이렉트)
    @GetMapping("/lectures")
    public String getLectures() {
        return "redirect:/instructor/mypage";
    }

    // 강의 상세 조회 페이지
    @GetMapping("/lectures/{lectureId}")
    public String getLecture(@PathVariable String lectureId, Model model) {
        System.out.println("=== 강의 상세 조회 시작 ===");
        System.out.println("Lecture ID: " + lectureId);

        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();
            System.out.println("Instructor ID: " + instructor.getInstructorId());

            LectureResponseDto lecture = instructorLectureService.getLectureByIdAndInstructor(
                    lectureId, instructor.getInstructorId());
            System.out.println("Lecture 조회 완료: " + lecture.getTitle());

            // 상세 정보도 조회
            try {
                LectureDetailResponseDto lectureDetail = instructorLectureService.getLectureDetailByIdAndInstructor(
                        lectureId, instructor.getInstructorId());
                System.out.println("LectureDetail 조회 완료");
                model.addAttribute("lectureDetail", lectureDetail);
            } catch (Exception e) {
                System.out.println("LectureDetail 조회 실패: " + e.getMessage());
                // lectureDetail이 없어도 계속 진행
            }

            model.addAttribute("lecture", lecture);

            System.out.println("=== 강의 상세 조회 완료 ===");
            return "instructor/lecture/detail";

        } catch (Exception e) {
            System.out.println("강의 조회 중 에러: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/lectures/new")
    public String createLectureForm(Model model) {
        System.out.println("=== 강의 생성 폼 표시 ===");

        try {
            // 빈 객체 생성 (null 값 방지)
            LectureRequestDto lectureRequest = new LectureRequestDto();

            // 기본값 설정
            lectureRequest.setDuration(120);  // 기본 2시간
            lectureRequest.setPrice(0);       // 기본 가격

            model.addAttribute("lectureRequest", lectureRequest);

            System.out.println("강의 생성 폼 준비 완료");
            return "instructor/lecture/form";

        } catch (Exception e) {
            System.out.println("강의 생성 폼 에러: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 새 강의 생성 처리
    @PostMapping("/lectures")
    public String createLecture(
            @ModelAttribute LectureRequestDto request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();

            // 이미지 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileUploadService.uploadLectureImage(imageFile, instructor.getInstructorId());
                request.setImageUrl(imageUrl);
            }

            // 강의 생성
            LectureResponseDto createdLecture = instructorLectureService.createLecture(request, instructor);

            redirectAttributes.addFlashAttribute("message", "강의가 성공적으로 생성되었습니다.");

            return "redirect:/instructor/lectures/" + createdLecture.getRawId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "강의 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/instructor/lectures/new";
        }
    }

    // 강의 수정 폼
    @GetMapping("/lectures/{lectureId}/edit")
    @Transactional(readOnly = true)  // 트랜잭션 추가
    public String updateLectureForm(@PathVariable String lectureId, Model model) {
        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();

            // 엔티티로 직접 조회 시 fetch join 사용
            Lecture lecture = instructorLectureService.getLectureEntityByIdAndInstructor(
                    lectureId, instructor.getInstructorId());

            // 트랜잭션 내에서 프록시 초기화를 위해 명시적으로 접근
            if (lecture.getInstructor() != null) {
                lecture.getInstructor().getNickname(); // 프록시 초기화
            }

            model.addAttribute("lecture", LectureResponseDto.fromEntity(lecture));
            model.addAttribute("lectureRequest", LectureRequestDto.fromEntity(lecture));
            return "instructor/lecture/edit";

        } catch (Exception e) {
            System.out.println("강의 수정 폼 로드 중 에러: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 강의 수정 처리
    @PostMapping("/lectures/{lectureId}")
    public String updateLecture(
            @PathVariable String lectureId,
            @ModelAttribute LectureRequestDto request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();

            // 기존 강의 정보 조회
            LectureResponseDto existingLecture = instructorLectureService.getLectureByIdAndInstructor(
                    lectureId, instructor.getInstructorId());

            // 이미지 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                // 기존 이미지 삭제
                if (existingLecture.getImageUrl() != null) {
                    fileUploadService.deleteImage(existingLecture.getImageUrl());
                }

                // 새 이미지 업로드
                String imageUrl = fileUploadService.uploadLectureImage(imageFile, instructor.getInstructorId());
                request.setImageUrl(imageUrl);
            } else {
                // 이미지가 없으면 기존 이미지 URL 유지
                request.setImageUrl(existingLecture.getImageUrl());
            }

            // 강의 수정
            LectureResponseDto updatedLecture = instructorLectureService.updateLecture(
                    lectureId, request, instructor.getInstructorId());

            redirectAttributes.addFlashAttribute("message", "강의가 성공적으로 수정되었습니다.");
            return "redirect:/instructor/lectures/" + updatedLecture.getRawId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "강의 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/instructor/lectures/" + lectureId + "/edit";
        }
    }

    // 강의 삭제 처리 (POST 방식)
    @PostMapping("/lectures/{lectureId}/delete")
    public String deleteLecture(
            @PathVariable String lectureId,
            RedirectAttributes redirectAttributes) {

        try {
            Instructor instructor = instructorAuthService.getCurrentInstructor();

            // 강의 정보 조회 (이미지 삭제를 위해)
            LectureResponseDto lecture = instructorLectureService.getLectureByIdAndInstructor(
                    lectureId, instructor.getInstructorId());

            // 이미지 삭제
            if (lecture.getImageUrl() != null) {
                fileUploadService.deleteImage(lecture.getImageUrl());
            }

            // 강의 삭제
            instructorLectureService.deleteLecture(lectureId, instructor.getInstructorId());

            redirectAttributes.addFlashAttribute("message", "강의가 성공적으로 삭제되었습니다.");
            return "redirect:/instructor/mypage";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "강의 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/instructor/lectures/" + lectureId;
        }
    }

    // === REST API 엔드포인트들 ===

    // 현재 로그인한 강사의 모든 강의 목록 조회
    @GetMapping("/api/lectures")
    @ResponseBody
    @Operation(summary = "강사 강의 목록 조회", description = "지정한 강사의 모든 강의 조회")
    public ResponseEntity<List<LectureResponseDto>> getMyLecturesApi() {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        List<LectureResponseDto> lectures = instructorLectureService.getLecturesByInstructor(instructor.getInstructorId());
        return ResponseEntity.ok(lectures);
    }

    // 강의 상세 정보 조회 (본인 강의만)
    @GetMapping("/api/lectures/{lectureId}")
    @ResponseBody
    @Operation(summary = "강의 상세 조회", description = "강사 ID와 강의 ID로 강의 상세 정보 조회")
    public ResponseEntity<LectureResponseDto> getLectureApi(@PathVariable String lectureId) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        LectureResponseDto lecture = instructorLectureService.getLectureByIdAndInstructor(
                lectureId, instructor.getInstructorId());
        return ResponseEntity.ok(lecture);
    }

    // 새 강의 생성
    @PostMapping("/api/lectures")
    @ResponseBody
    @Operation(summary = "새 강의 생성", description = "지정한 강사로 새 강의 생성")
    public ResponseEntity<LectureResponseDto> createLectureApi(@RequestBody LectureRequestDto request) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        LectureResponseDto createdLecture = instructorLectureService.createLecture(request, instructor);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLecture);
    }

    // 기존 강의 수정 (본인 강의만)
    @PutMapping("/api/lectures/{lectureId}")
    @ResponseBody
    @Operation(summary = "강의 수정", description = "강사 ID와 강의 ID로 강의 정보 수정")
    public ResponseEntity<LectureResponseDto> updateLectureApi(
            @PathVariable String lectureId,
            @RequestBody LectureRequestDto request) {

        Instructor instructor = instructorAuthService.getCurrentInstructor();
        LectureResponseDto updatedLecture = instructorLectureService.updateLecture(
                lectureId, request, instructor.getInstructorId());
        return ResponseEntity.ok(updatedLecture);
    }

    // 강의 삭제 (본인 강의만)
    @DeleteMapping("/api/lectures/{lectureId}")
    @ResponseBody
    @Operation(summary = "강의 삭제", description = "강의 삭제 (본인 강의만)")
    public ResponseEntity<Void> deleteLectureApi(@PathVariable String lectureId) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        instructorLectureService.deleteLecture(lectureId, instructor.getInstructorId());
        return ResponseEntity.noContent().build();
    }

    // 강의 활성화/비활성화 토글 (본인 강의만)
    @PatchMapping("/api/lectures/{lectureId}/toggle-active")
    @ResponseBody
    @Operation(summary = "활성화/비활성화 토글", description = "강의 활성화 상태 토글 (본인 강의만)")
    public ResponseEntity<LectureResponseDto> toggleLectureActiveApi(@PathVariable String lectureId) {
        Instructor instructor = instructorAuthService.getCurrentInstructor();
        LectureResponseDto lecture = instructorLectureService.toggleLectureActive(
                lectureId, instructor.getInstructorId());
        return ResponseEntity.ok(lecture);
    }
}