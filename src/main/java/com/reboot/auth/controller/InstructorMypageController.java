package com.reboot.auth.controller;

import com.reboot.auth.dto.InstructorProfileDTO;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.InstructorMypageService;
import com.reboot.lecture.service.InstructorLectureService;
import com.reboot.lecture.dto.LectureResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
public class InstructorMypageController {
    private final InstructorMypageService instructorMypageService;
    private final InstructorLectureService instructorLectureService;  // 추가

    public InstructorMypageController(InstructorMypageService instructorMypageService,
                                      InstructorLectureService instructorLectureService) {  // 수정
        this.instructorMypageService = instructorMypageService;
        this.instructorLectureService = instructorLectureService;  // 추가
    }

    @GetMapping("/instructorMypage")
    public String instructorMyPage(Principal principal, HttpServletRequest request, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        // 강사가 아닌 경우 일반 마이페이지로 리다이렉트
        if (!instructorMypageService.isInstructor(principal.getName())) {
            return "redirect:/mypage";
        }

        Instructor instructor = instructorMypageService.getInstructor(principal.getName());

        // 강의 목록 추가
        List<LectureResponseDto> lectures = instructorLectureService.getLecturesByInstructor(instructor.getInstructorId());

        model.addAttribute("instructor", instructor);
        model.addAttribute("member", instructor.getMember());
        model.addAttribute("lectures", lectures);

        return "mypage/instructorMypage";
        }
    // 강사 프로필 수정 페이지
    @GetMapping("/instructor/profile")
    public String instructorProfileForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        // 강사가 아닌 경우 일반 마이페이지로 리다이렉트
        if (!instructorMypageService.isInstructor(principal.getName())) {
            return "redirect:/mypage";
        }

        Instructor instructor = instructorMypageService.getInstructor(principal.getName());
        InstructorProfileDTO instructorProfile = instructorMypageService.getCurrentInstructorProfile(principal.getName());

        model.addAttribute("instructor", instructor);
        model.addAttribute("member", instructor.getMember());
        model.addAttribute("instructorProfileDTO", instructorProfile);

        return "mypage/InstructorProfile-edit";
    }
    // 강사 프로필 수정 처리
    @PostMapping("/instructor/profile")
    public String updateInstructorProfile(@ModelAttribute InstructorProfileDTO instructorProfileDTO,
                                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        try {
            instructorMypageService.updateInstructorProfile(principal.getName(), instructorProfileDTO, profileImage);
            redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/instructor/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/instructor/profile";
        }

        return "redirect:/mypage/instructorMypage";
    }
}