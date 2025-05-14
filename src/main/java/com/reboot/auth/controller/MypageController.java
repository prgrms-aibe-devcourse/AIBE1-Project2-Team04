package com.reboot.auth.controller;

import com.reboot.auth.dto.GameDTO;
import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.service.MypageService;
import com.reboot.payment.entity.Payment;
import com.reboot.reservation.entity.Reservation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final MypageService mypageService;

    // 생성자 수정 - @Autowired 어노테이션 제거 (Spring이 자동으로 주입)
    public MypageController(MemberRepository memberRepository,
                            GameRepository gameRepository,
                            MypageService mypageService) {
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
        this.mypageService = mypageService;
    }

    // 메인 마이페이지 (일반 사용자) - 단순화 버전
    @GetMapping
    public String mypage(Principal principal, Model model) {
        try {
            // 인증 확인
            if (principal == null) {
                return "redirect:/auth/login";
            }

            // 강사인 경우 강사 페이지로 자동 리다이렉트
            if (mypageService.isInstructor(principal.getName())) {
                return "redirect:/mypage/instructorMypage";
            }

            // 기본 정보 조회
            Member member = mypageService.getCurrentMember(principal.getName());
            List<Game> game = gameRepository.findByMember_MemberId(member.getMemberId());

            // 예약 및 결제 정보 조회 (단순화)
            List<Reservation> pendingReservations = mypageService.getPendingReservations(principal.getName());
            List<Payment> completedPayments = mypageService.getCompletedPayments(principal.getName());
            List<Reservation> completedReservations = mypageService.getCompletedReservations(principal.getName());

            // 모델에 데이터 설정
            model.addAttribute("member", member);
            model.addAttribute("game", game);
            model.addAttribute("pendingReservations", pendingReservations);
            model.addAttribute("completedPayments", completedPayments);
            model.addAttribute("completedReservations", completedReservations);

            // 디버깅 로그
            System.out.println("=== 단순화된 마이페이지 데이터 ===");
            System.out.println("결제 대기 예약 수: " + pendingReservations.size());
            System.out.println("결제 완료 Payment 수: " + completedPayments.size());
            System.out.println("결제 완료 예약 수: " + completedReservations.size());

            return "mypage/index";

        } catch (Exception e) {
            System.err.println("마이페이지 접속 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/error";
        }
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
                .email(member.getEmail())       // 읽기 전용
                .nickname(member.getNickname()) // 변경 가능
                .phone(member.getPhone())       // 변경 가능
                .build();

        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("member", member);
        return "mypage/profile-edit";
    }

    // 프로필 정보 업데이트
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileDTO profileDTO,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        // 파일 크기 사전 검증
        if (profileImage != null && !profileImage.isEmpty()) {
            if (profileImage.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "프로필 이미지는 5MB 이하여야 합니다.");
                return "redirect:/mypage/profile";
            }

            String contentType = profileImage.getContentType();
            if (contentType == null || !(contentType.equals("image/jpeg") ||
                    contentType.equals("image/png") ||
                    contentType.equals("image/gif"))) {
                redirectAttributes.addFlashAttribute("error", "JPG, PNG, GIF 형식의 이미지만 허용됩니다.");
                return "redirect:/mypage/profile";
            }
        }

        try {
            mypageService.updateProfile(principal.getName(), profileDTO, profileImage);
            redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/profile";
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

    //게임정보
    @GetMapping("/game")
    public String myGames(Principal principal, Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        List<Game> game = gameRepository.findByMember_MemberId(member.getMemberId());

        model.addAttribute("member", member);
        model.addAttribute("game", game);
        return "mypage/game";
    }

    @GetMapping("/game/register")
    public String gameRegisterForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        if (mypageService.hasGameInfo(principal.getName())) {
            return "redirect:/mypage/game/edit";
        }

        model.addAttribute("gameDTO", new GameDTO());
        return "mypage/game-register";
    }

    @PostMapping("/game/register")
    public String gameRegister(@ModelAttribute GameDTO gameDTO,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            mypageService.saveGameInfo(principal.getName(), gameDTO);
            redirectAttributes.addFlashAttribute("message", "게임 정보가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게임 정보 등록 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage";
    }

    @GetMapping("/game/edit")
    public String gameEditForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        if (!mypageService.hasGameInfo(principal.getName())) {
            return "redirect:/mypage/game/register";
        }

        Game game = mypageService.getCurrentGameByMember(principal.getName());
        GameDTO gameDTO = GameDTO.builder()
                .gameType(game.getGameType())
                .gameTier(game.getGameTier())
                .gamePosition(game.getGamePosition())
                .build();

        model.addAttribute("gameDTO", gameDTO);
        return "mypage/game-edit";
    }

    @PostMapping("/game/edit")
    public String gameEdit(@ModelAttribute GameDTO gameDTO,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            mypageService.updateGameInfo(principal.getName(), gameDTO);
            redirectAttributes.addFlashAttribute("message", "게임 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게임 정보 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage";
    }

    // 결제 상태 확인 API (디버깅용) - ResponseBody import 필요
    @GetMapping("/check-payment/{reservationId}")
    @ResponseBody
    public String checkPayment(@PathVariable Long reservationId) {
        boolean hasPayment = mypageService.hasPayment(reservationId);
        return "예약 ID " + reservationId + " 결제 상태: " + (hasPayment ? "완료" : "대기");
    }
}