package com.reboot.auth.service;

import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.dto.GameDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.entity.ReservationMy;
import com.reboot.auth.repository.*;
import com.reboot.payment.entity.Payment;
import com.reboot.payment.repository.PaymentRepository;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.service.ReservationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.reboot.reservation.service.ReservationService;
import com.reboot.reservation.dto.ReservationResponseDto;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MypageService {

    private final MemberRepository memberRepository;
    private final ReservationMyRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private GameRepository gameRepository;

//    @Autowired
//    private com.reboot.reservation.repository.ReservationRepository mainReservationRepository;
    @Autowired
    private ReservationService reservationService;


    public MypageService(MemberRepository memberRepository,
                         ReservationMyRepository reservationRepository,
                         PasswordEncoder passwordEncoder,
                         FileUploadService fileUploadService,
                         ReservationService reservationService) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
        this.reservationService = reservationService;
    }

    public Member getCurrentMember(String username) {
        return memberRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 프로필 업데이트
    @Transactional
    public void updateProfile(String username, ProfileDTO profileDTO, MultipartFile profileImage) throws IOException {
        Member member = getCurrentMember(username);

        //Nickname, Phone, Image 만 변경 가능
        member.setNickname(profileDTO.getNickname());
        member.setPhone(profileDTO.getPhone());

        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            // 이미지 유효성 검사
            validateProfileImage(profileImage);

            // Supabase에 이미지 업로드
            String imageUrl = fileUploadService.uploadImageToSupabase(profileImage);
            if (imageUrl != null) {
                member.setProfileImage(imageUrl);
            }
        }

        // 저장
        memberRepository.save(member);
    }

    // 프로필 이미지 파일 유효성 검사
    private void validateProfileImage(MultipartFile file) {
        // 파일 크기 확인 (5MB 제한)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 파일 형식 확인
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif"))) {
            throw new IllegalArgumentException("JPG, PNG, GIF 형식의 이미지만 허용됩니다.");
        }
    }

    // 비밀번호 변경
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Member member = getCurrentMember(username);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            return false;
        }

        // 새 비밀번호 암호화 및 저장
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        return true;
    }

    // 결제 대기 중인 예약 조회
    public List<ReservationMy> getPendingMyReservations(String username) {
        Member member = getCurrentMember(username);

        // 1. 먼저 메인 시스템에서 누락된 예약들을 동기화
        syncReservationsFromMainSystem(member.getMemberId());

        // 2. ReservationMy에서 결제 대기 중인 예약 조회
        List<ReservationMy> allReservations = reservationRepository.findByMemberId(member.getMemberId());

        return allReservations.stream()
                .filter(reservation -> {
                    // 여러 상태 패턴 지원
                    String status = reservation.getStatus();
                    return "예약완료".equals(status) || "COMPLETED".equals(status) || "PENDING".equals(status);
                })
                .filter(reservation -> !this.hasPayment(reservation.getId()))
                .collect(Collectors.toList());
    }

    // 메인 시스템과 동기화
    @Transactional
    protected void syncReservationsFromMainSystem(Long memberId) {
        try {
            // 메인 Reservation 시스템에서 예약 조회
            List<ReservationResponseDto> mainReservations = reservationService.getReservationsByMember(memberId);

            for (ReservationResponseDto mainRes : mainReservations) {
                // ReservationMy에 없는 예약이 있으면 생성
                if (!reservationRepository.existsById(mainRes.getReservationId())) {
                    ReservationMy newReservationMy = ReservationMy.builder()
                            .id(mainRes.getReservationId())
                            .memberId(memberId)
                            .instructorId(mainRes.getInstructorId())
                            .lectureId(mainRes.getLectureId())
                            .date(mainRes.getDate())
                            .status(mainRes.getStatus())
                            .build();

                    reservationRepository.save(newReservationMy);
                    System.out.println("동기화: 새로운 예약 추가 - ID: " + newReservationMy.getId());
                }

                // 결제 상태도 확인하여 업데이트
                if (this.hasPayment(mainRes.getReservationId())) {
                    Optional<ReservationMy> reservationMyOpt = reservationRepository.findById(mainRes.getReservationId());
                    if (reservationMyOpt.isPresent() && !"결제완료".equals(reservationMyOpt.get().getStatus())) {
                        ReservationMy reservationMy = reservationMyOpt.get();
                        reservationMy.setStatus("결제완료");
                        reservationRepository.save(reservationMy);
                        System.out.println("동기화: 결제 상태 업데이트 - ID: " + reservationMy.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("예약 동기화 중 오류: " + e.getMessage());
            e.printStackTrace();
            // 오류가 있어도 계속 진행
        }
    }

    // 상태 매핑 메서드
    private String mapReservationStatus(String mainStatus) {
        if (mainStatus == null) return "예약완료";

        switch (mainStatus) {
            case "COMPLETED":
            case "예약완료":
                return "예약완료";
            case "PAID":
            case "결제완료":
                return "결제완료";
            case "CANCELLED":
            case "취소":
                return "취소";
            default:
                return "예약완료"; // 기본값
        }
    }

    // 커스텀 결제 완료 조회 메서드
    public List<Payment> getCompletedPayments(String username) {
        Member member = getCurrentMember(username);

        // 예약 동기화 먼저 실행
        syncReservationsFromMainSystem(member.getMemberId());

        // 전체 조회 후 필터링
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getReservation() != null)
                .filter(payment -> payment.getReservation().getMember() != null)
                .filter(payment -> payment.getReservation().getMember().getMemberId().equals(member.getMemberId()))
                .filter(payment -> "결제완료".equals(payment.getStatus())) // '결제완료'로 필터링
                .collect(Collectors.toList());
    }

    public boolean hasPayment(Long reservationId) {
        // 특정 예약에 대한 결제가 있는지 확인
        return paymentRepository.findAll().stream()
                .anyMatch(payment -> payment.getReservation() != null &&
                        payment.getReservation().getReservationId().equals(reservationId) &&
                        "결제완료".equals(payment.getStatus()));
    }

        // 강사 인증 확인
    public boolean isInstructor(String username) {
        Member member = getCurrentMember(username);
        return instructorRepository.existsByMember(member);
    }

    public Instructor getInstructorByMember(String username) {
        Member member = getCurrentMember(username);
        return instructorRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("강사 정보를 찾을 수 없습니다."));
    }

    public boolean hasGameInfo(String username) {
        Member member = getCurrentMember(username);
        return gameRepository.existsByMemberMemberId(member.getMemberId());
    }

    @Transactional
    public void saveGameInfo(String username, GameDTO gameDTO) {
        Member member = getCurrentMember(username);

        Game game = new Game();
        game.setMember(member);
        game.setGameType(gameDTO.getGameType());
        game.setGameTier(gameDTO.getGameTier());
        game.setGamePosition(gameDTO.getGamePosition());

        gameRepository.save(game);
    }

    public Game getCurrentGameByMember(String username) {
        Member member = getCurrentMember(username);

        List<Game> games = gameRepository.findByMember_MemberId(member.getMemberId());
        if (games.isEmpty()) {
            throw new RuntimeException("게임 정보를 찾을 수 없습니다.");
        }
        return games.get(0); // 첫 번째 게임 정보 반환
    }

    @Transactional
    public void updateGameInfo(String username, GameDTO gameDTO) {
        Member member = getCurrentMember(username);

        List<Game> games = gameRepository.findByMember_MemberId(member.getMemberId());
        if (games.isEmpty()) {
            throw new RuntimeException("게임 정보를 찾을 수 없습니다.");
        }

        Game game = games.get(0); // 첫 번째 게임 정보 업데이트
        game.setGameType(gameDTO.getGameType());
        game.setGameTier(gameDTO.getGameTier());
        game.setGamePosition(gameDTO.getGamePosition());

        gameRepository.save(game);
    }
}