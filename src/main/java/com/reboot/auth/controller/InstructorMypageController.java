package com.reboot.auth.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.InstructorMypageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/mypage")
public class InstructorMypageController {
    private final InstructorMypageService instructorMypageService;

    public InstructorMypageController(InstructorMypageService instructorMypageService) {
        this.instructorMypageService = instructorMypageService;
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

        model.addAttribute("instructor", instructor);
        model.addAttribute("member", instructor.getMember());

        return "mypage/instructorMypage";
    }
}
