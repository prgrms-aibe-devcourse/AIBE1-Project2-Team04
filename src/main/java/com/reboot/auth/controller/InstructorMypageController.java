package com.reboot.auth.controller;

import com.reboot.auth.dto.InstructorProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.service.InstructorMypageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Member member = instructor.getMember();
        Game game = member.getGame();

        model.addAttribute("game", game);
        model.addAttribute("instructor", instructor);
        model.addAttribute("member", instructor.getMember());

        return "mypage/instructorMypage";
    }

    //프로필 수정
    @GetMapping("/profile")
    public String instructorProfileForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        if (!instructorMypageService.isInstructor(principal.getName())) {
            return "redirect:/mypage";
        }

        // 현재 상태 조회
        Instructor instructor = instructorMypageService.getInstructor(principal.getName());
        Member member = instructor.getMember();
        InstructorProfileDTO dto = instructorMypageService.getCurrentInstructorProfile(principal.getName());

        model.addAttribute("instructorProfileDTO", dto);
        model.addAttribute("member", member);
        model.addAttribute("instructor", instructor);

        // 추가 디버깅 로그
        System.out.println("=== 컨트롤러 디버깅 ===");
        System.out.println("member: " + member);
        System.out.println("member.nickname: " + (member != null ? member.getNickname() : "null"));
        System.out.println("member.phone: " + (member != null ? member.getPhone() : "null"));
        System.out.println("instructor: " + instructor);
        System.out.println("instructor.career: " + (instructor != null ? instructor.getCareer() : "null"));
        System.out.println("instructor.description: " + (instructor != null ? instructor.getDescription() : "null"));

        Game game = member.getGame();
        System.out.println("game: " + game);
        System.out.println("game.gameType: " + (game != null ? game.getGameType() : "null"));

        System.out.println("=== DTO 내용 ===");
        System.out.println("dto: " + dto);
        System.out.println("dto.nickname: " + dto.getNickname());
        System.out.println("dto.phone: " + dto.getPhone());


        return "mypage/instructorProfile-edit";
    }

    @PostMapping("/profile")
    public String updateInstructorProfile(@ModelAttribute @Valid InstructorProfileDTO dto,
                                          BindingResult bindingResult,
                                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            return "redirect:/mypage/profile";
        }

        try {
            instructorMypageService.updateInstructorProfile(principal.getName(), dto, profileImage);
            redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "프로필 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/profile";
        }
        return "redirect:/mypage/instructorMypage";
    }
}
