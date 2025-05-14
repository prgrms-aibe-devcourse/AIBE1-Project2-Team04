package com.reboot.auth.controller;

import com.reboot.auth.dto.GameDTO;
import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.entity.ReservationMy;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.ReservationMyRepository;
import com.reboot.auth.service.MypageService;
import com.reboot.payment.entity.Payment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
                            MypageService mypageService) {
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
        this.reservationMyRepository = reservationMyRepository;
        this.mypageService = mypageService;
    }

    // 메인 마이페이지 (일반 사용자)
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

            // 1. 강제 동기화 실행 (결제 완료 후 반영 확인)
            mypageService.forceSync(principal.getName());

            // 2. 동기화 후 데이터 재조회
            List<ReservationMy> reservations = reservationMyRepository.findByMemberId(member.getMemberId());

            // 3. 결제 대기 중인 예약 조회
            List<ReservationMy> pendingReservations = mypageService.getPendingMyReservations(principal.getName());

            // 4. 결제 완료된 Payment 목록 조회
            List<Payment> completedPayments = mypageService.getCompletedPayments(principal.getName());

            // 5. 결제 완료된 강의(ReservationMy) 목록 조회
            List<ReservationMy> completedCourses = mypageService.getCompletedCourses(principal.getName());

            // 모델에 데이터 설정 (HTML 템플릿 변수명과 정확히 일치)
            model.addAttribute("member", member);
            model.addAttribute("game", game);
            model.addAttribute("reservations", reservations); // 전체 예약 (미사용이지만 유지)
            model.addAttribute("pendingReservations", pendingReservations); // 결제 대기
            model.addAttribute("completedPayments", completedPayments); // 결제 완료
            model.addAttribute("completedCourses", completedCourses); // 수강 완료

            // 디버깅 로그
            System.out.println("=== 마이페이지 데이터 확인 ===");
            System.out.println("전체 예약 수: " + reservations.size());
            System.out.println("결제 대기 예약 수: " + pendingReservations.size());
            System.out.println("결제 완료 Payment 수: " + completedPayments.size());
            System.out.println("수강 완료 강의 수: " + completedCourses.size());

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
        List<ReservationMy> reservations = reservationMyRepository.findByMemberId(member.getMemberId());

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
        Optional<ReservationMy> reservation = reservationMyRepository.findById(reservationId);

        if (reservation.isPresent() && reservation.get().getMemberId().equals(member.getMemberId())) {
            model.addAttribute("member", member);
            model.addAttribute("reservation", reservation.get());
            return "mypage/reservation-detail";
        } else {
            return "redirect:/mypage/reservations";
        }
    }

    // 강제 동기화 API (디버깅용)
    @PostMapping("/sync")
    @ResponseBody
    public String forceSync(Principal principal) {
        try {
            if (principal == null) {
                return "로그인이 필요합니다.";
            }

            mypageService.forceSync(principal.getName());
            return "동기화 완료";
        } catch (Exception e) {
            return "동기화 실패: " + e.getMessage();
        }
    }

    // 테이블 상태 비교 API (디버깅용)
    @GetMapping("/compare")
    @ResponseBody
    public String compareTables(Principal principal) {
        try {
            if (principal == null) {
                return "로그인이 필요합니다.";
            }

            mypageService.compareTables(principal.getName());
            return "테이블 비교 완료 (콘솔 로그 확인)";
        } catch (Exception e) {
            return "테이블 비교 실패: " + e.getMessage();
        }
    }
}