package com.reboot.auth.controller;

import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.entity.Reservation;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.ReservationRepository;
import com.reboot.auth.service.MypageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.reboot.auth.repository.ReservationRepository;
import com.reboot.auth.entity.Reservation;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MemberRepository memberRepository;
    // 매칭
    private final ReservationRepository reservationRepository;
    private final GameRepository gameRepository;
    private final MypageService mypageService;

    public MypageController(MemberRepository memberRepository,
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
        Member member = mypageService.getCurrentMember(principal.getName());
        List<Game> games = GameRepository.findByMemberId(member.getMemberId());
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("games", games);
        model.addAttribute("reservations",reservations);

        return "mypage";
    }

    //프로필 수정 페이지
    @GetMapping("/profile")
    public String profileEditForm(Principal principal, Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        model.addAttribute("member",member);
        return "mypage/profile-edit";
    }

    // 프로필 정보 업데이트
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileDTO profileDTO,
                                @RequestParam(value = "profileImage", required = false)MultipartFile profileImage,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            mypageService.updateProfile(principal.getName(), profileDTO);
            redirectAttributes.addAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "프로필 업데이트 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage";
    }

    //비밀번호 변경
    @PostMapping("/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage/password";
        }

        try {
            boolean success = mypageService.changePassword(principal.getName(), currentPassword, newPassword);
            if (success) {
                redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
                return "redirect:/mypage";
            } else {
                redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 올바르지 않습니다.");
                return "redirect:/mypage/password";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/password";
        }
    }

    //개인정보 관리

    //게임정보 (읽기전용)
    @GetMapping("/game")
    public String myGames(Principal principal, Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        List<Game> game = GameRepository.findByMemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("game", game);
        return "mypage/game";
    }

    //수강신청 내역 페이지
    @GetMapping("/reservations")
    public String myReservations(Principal principal, Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("reservations", reservations);

        return "mypage/reservations";
    }

    //수강신청 상세정보
    @GetMapping("/reservations/{reservationId}")
    public String reservationDetail(@PathVariable Long reservationId,
                                    Principal principal,
                                    Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);

        if (reservation.isPresent() && reservation.get().getMemberId().equals(member.getMemberId())) {
            model.addAttribute("member", member);
            model.addAttribute("reservation", reservation.get());
            return "mypage/reservation-detail";
        } else {
            return "redirect:/mypage/reservations";
        }
    }
}