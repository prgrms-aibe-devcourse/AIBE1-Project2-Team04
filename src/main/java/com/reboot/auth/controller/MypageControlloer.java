package com.reboot.auth.controller;

import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.service.MypageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
public class MypageControlloer {

    private final MemberRepository memberRepository;
    // 매칭
    private final ReservationRepository reservationRepository;
    private final GameRepository gameRepository;
    private final MypageService mypageService;

    public MypageControlloer(MemberRepository memberRepository,
                             GameRepository gameRepository,
                             ReservationRepository reservationRepository,
                             MypageService mypageService) {
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
        this.reservationRepository = reservationRepository;
        this.mypageService = mypageService;
    }

    // 메인화면
    @GetMapping
    public String mypage(Principal principal, Model model) {
        // 로그인 사용자 정보 조회
        Member meber = mypageService.getCurrentMember(principal.getName());
        List<Game> games = GameRepository.findByMemberId(member.getMemberId());
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("games", games);
        model.addAttribute("reservations",reservations);
    }

    //프로필 수정 페이지
    @GetMapping("/profile")
    public String profileEditForm(Principal principal, Model model) {
        Member member = mypageSercice.getCurrentMember(principal.getName());
        model.addAttriboute("member",member);
        return "mypage/profile-deit";
    }

    // 프로필 정보 업데이트
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileUpdateDTO  profileDTO,
                                @RequestParam(value = "profileImage", required = false)MultipartFile profileImage,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            mypageService.updateProfile(principal.getName(), profileDTO, profileImage);
            redirectAttributes.addAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "프로필 업데이트 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage";
    }

    //비밀번호 변경 ??

    //개인정보 관리

    //게임 정보 업데이트(추가) ??

    //게임 정보 삭제

    //수강신청 내역 페이지

    //수강신청 상세정보

    //수강신청 취소 페이지












}
