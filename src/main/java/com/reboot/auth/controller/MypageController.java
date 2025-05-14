package com.reboot.auth.controller;

import com.reboot.auth.dto.GameDTO;
import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.entity.ReservationMy;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.ReservationMyRepository;
import com.reboot.auth.service.MypageService;
import com.reboot.payment.entity.Payment;
import com.reboot.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MemberRepository memberRepository;
    private final ReservationMyRepository reservationMyRepository;
    private final GameRepository gameRepository;
    private final MypageService mypageService;

    public MypageController(MemberRepository memberRepository,
                            GameRepository gameRepository,
                            ReservationMyRepository reservationMyRepository,
                            MypageService mypageService,
                            ReservationRepository mainReservationRepository) {
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
        this.reservationMyRepository = reservationMyRepository;
        this.mypageService = mypageService;
    }

    // 메인화면
    @GetMapping
    public String mypage(Principal principal, Model model) {

        // 인증 확인
        if (principal == null) {
            return "redirect:/auth/login";  // 로그인 페이지로 리다이렉트
        }

        // 강사인 경우 강사 페이지로 자동 리다이렉트
        if (mypageService.isInstructor(principal.getName())) {
            return "redirect:/mypage/instructorMypage";
        }

        // 결제 대기 중인 예약 동기화
        List<ReservationMy> pendingReservations = mypageService.getPendingMyReservations(principal.getName());

        // 결제가 완료된 예약들 상태 업데이트
        for (ReservationMy reservation : pendingReservations) {
            if (mypageService.hasPayment(reservation.getId())) {
                updateReservationMyStatusToCompleted(reservation.getId());
            }
        }

        // 로그인 사용자 정보 조회
        Member member = mypageService.getCurrentMember(principal.getName());
        List<Game> game = gameRepository.findByMember_MemberId(member.getMemberId());//수정
        List<ReservationMy> reservationMIES = reservationMyRepository.findByMemberId(member.getMemberId());

        // 업데이트 후 다시 조회
        pendingReservations = mypageService.getPendingMyReservations(principal.getName());
        List<Payment> completedPayments = mypageService.getCompletedPayments(principal.getName());

        model.addAttribute("member", member);
        model.addAttribute("game", game);
        model.addAttribute("reservations", reservationMIES);
        model.addAttribute("completedPayments", completedPayments);
        model.addAttribute("pendingReservations", pendingReservations);

//        // 강사 인증 확인
//        boolean isInstructor = mypageService.isInstructor(principal.getName());
//        model.addAttribute("isInstructor", isInstructor);

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
        // 파일 크기 사전 검증 추가
        if (profileImage != null && !profileImage.isEmpty()) {
            // 5MB 제한
            if (profileImage.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "프로필 이미지는 5MB 이하여야 합니다.");
                return "redirect:/mypage/profile";
            }

            // 파일 형식 검증
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

        // 이미 게임 정보가 있으면 수정 페이지로 리다이렉트
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

        // 게임 정보가 없으면 등록 페이지로 리다이렉트
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

    //수강신청 내역 페이지
    @GetMapping("/reservations")
    public String myReservations(Principal principal, Model model) {
        Member member = mypageService.getCurrentMember(principal.getName());
        List<ReservationMy> reservationMIES = reservationMyRepository.findByMemberId(member.getMemberId());

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
        Optional<ReservationMy> reservation = reservationMyRepository.findById(reservationId);

        if (reservation.isPresent() && reservation.get().getMemberId().equals(member.getMemberId())) {
            model.addAttribute("member", member);
            model.addAttribute("reservation", reservation.get());
            return "mypage/reservation-detail";
        } else {
            return "redirect:/mypage/reservations";
        }
    }

    // 예약 상태 업데이트 헬퍼 메서드
    private void updateReservationMyStatusToCompleted(Long reservationId) {
        try {
            Optional<ReservationMy> reservationMyOpt = reservationMyRepository.findById(reservationId);
            if (reservationMyOpt.isPresent()) {
                ReservationMy reservationMy = reservationMyOpt.get();
                reservationMy.setStatus("결제완료");
                reservationMyRepository.save(reservationMy);
            }
        } catch (Exception e) {
            System.err.println("예약 상태 업데이트 실패: " + e.getMessage());
        }
    }
}