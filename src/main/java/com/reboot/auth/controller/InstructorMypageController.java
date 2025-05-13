package com.reboot.auth.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.InstructorMypageService;
import com.reboot.lecture.service.InstructorLectureService;
import com.reboot.lecture.dto.LectureResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("lectures", lectures);  // 이 줄 추가

        return "mypage/instructorMypage";
    }
}