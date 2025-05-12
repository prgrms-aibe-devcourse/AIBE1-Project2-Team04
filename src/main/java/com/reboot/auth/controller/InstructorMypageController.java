package com.reboot.auth.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.service.InstructorMypageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/instructor/mypage")
public class InstructorMypageController {
    private final InstructorMypageService instructorMypageService;

    public InstructorMypageController(InstructorMypageService instructorMypageService) {
        this.instructorMypageService = instructorMypageService;
    }

    @GetMapping
    public String instructorMypage(Principal principal, Model model) {
        // 강사 인증 확인
        if (!instructorMypageService.isInstructor(principal.getName())) {
            return "redirect:/mypage?error=require-instructor-auth";
        }

        Instructor instructor = instructorMypageService.getInstructor(principal.getName());

        model.addAttribute("instructor", instructor);
        model.addAttribute("member", instructor.getMember());

        return "mypage/instructorMypage";
    }
}
