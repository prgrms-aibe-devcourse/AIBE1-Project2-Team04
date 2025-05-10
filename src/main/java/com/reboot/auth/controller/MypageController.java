package com.reboot.auth.controller;

import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.entity.ReservationMy;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.ReservationRepository;
import com.reboot.auth.service.MypageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// import com.reboot.auth.entity.Reservation;

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

        // 인증 확인
        if (principal == null) {
            return "redirect:/auth/login";  // 로그인 페이지로 리다이렉트
        }

        // 로그인 사용자 정보 조회
        Member member = mypageService.getCurrentMember(principal.getName());
        List<Game> games = gameRepository.findByMember_MemberId(member.getMemberId());//수정
        List<ReservationMy> reservationMIES = reservationRepository.findByMemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("games", games);
        model.addAttribute("reservations", reservationMIES);

        return "mypage/index";
    }

    //프로필 수정 페이지
    @GetMapping("/profile")
    public String profileEditForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Member member = mypageService.getCurrentMember(principal.getName());

        ProfileDTO profileDTO = ProfileDTO.builder()
                .username(member.getUsername()) // 읽기 전용
                .name(member.getName())         // 읽기 전용
                .email(member.getEmail())       // 일기 전용
                .nickname(member.getNickname()) // 변경 가능
                .phone(member.getPhone())       // 변경 가능
                .build();

        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("member",member);
        return "mypage/profile-edit";
    }

    // 프로필 정보 업데이트
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileDTO profileDTO,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        // 간단한 유효성 검사
        if (profileDTO.getNickname() == null || profileDTO.getNickname().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "닉네임은 필수 입력 항목입니다.");
            return "redirect:/mypage/profile";
        }

        try {
            mypageService.updateProfile(principal.getName(), profileDTO, profileImage);
            redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mypage";
    }

    //비밀번호 변경
    @GetMapping("/password")
    public String passwordChangeForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Member member = mypageService.getCurrentMember(principal.getName());
        model.addAttribute("member", member);
        return "mypage/password-change";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        // 비밀번호 유효성 검사
        if (newPassword == null || newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호는 최소 8자 이상이어야 합니다.");
            return "redirect:/mypage/password";
        }

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
        List<Game> game = gameRepository.findByMember_MemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("game", game);
        return "mypage/game";
    }

    //수강신청 내역 페이지
    @GetMapping("/reservations")
    public String myReservations(Principal principal, Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        List<ReservationMy> reservationMIES = reservationRepository.findByMemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("reservations", reservationMIES);

        return "mypage/reservations";
    }

    //수강신청 상세정보
    @GetMapping("/reservations/{reservationId}")
    public String reservationDetail(@PathVariable Long reservationId,
                                    Principal principal,
                                    Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        Optional<ReservationMy> reservation = reservationRepository.findById(reservationId);

        if (reservation.isPresent() && reservation.get().getMemberId().equals(member.getMemberId())) {
            model.addAttribute("member", member);
            model.addAttribute("reservation", reservation.get());
            return "mypage/reservation-detail";
        } else {
            return "redirect:/mypage/reservations";
        }
    }
}
