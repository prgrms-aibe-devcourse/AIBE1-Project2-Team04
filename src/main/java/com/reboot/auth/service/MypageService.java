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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MypageService {

    private final MemberRepository memberRepository;
    // 필드명을 더 명확하게 수정
    private final ReservationMyRepository reservationMyRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ReservationService reservationService;

    public MypageService(MemberRepository memberRepository,
                         ReservationMyRepository reservationMyRepository,
                         PasswordEncoder passwordEncoder,
                         FileUploadService fileUploadService,
                         ReservationService reservationService) {
        this.memberRepository = memberRepository;
        this.reservationMyRepository = reservationMyRepository;
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

    //두 테이블 상태 비교 및 출력 (디버깅용)
    public void compareTables(String username) {
        Member member = getCurrentMember(username);
        Long memberId = member.getMemberId();

        System.out.println("=== 테이블 상태 비교 ===");
        System.out.println("Member ID: " + memberId);

        try {
            // reservation 테이블 조회 (메인 시스템)
            List<ReservationResponseDto> mainReservations = reservationService.getReservationsByMember(memberId);
            System.out.println("\n[reservation 테이블 - 메인 시스템]");
            System.out.println("총 예약 수: " + mainReservations.size());
            for (ReservationResponseDto res : mainReservations) {
                boolean hasPayment = checkPaymentStatus(res.getReservationId());
                System.out.println("  - ID: " + res.getReservationId() +
                        ", 상태: " + res.getStatus() +
                        ", 결제상태: " + (hasPayment ? "완료" : "미완료"));
            }

            // reservations 테이블 조회 (마이페이지 시스템)
            List<ReservationMy> myReservations = reservationMyRepository.findByMemberId(memberId);
            System.out.println("\n[reservations 테이블 - 마이페이지 시스템]");
            System.out.println("총 예약 수: " + myReservations.size());
            for (ReservationMy res : myReservations) {
                System.out.println("  - ID: " + res.getId() +
                        ", 상태: " + res.getStatus());
            }

            // 누락된 예약 찾기
            System.out.println("\n[동기화 필요한 예약]");
            for (ReservationResponseDto mainRes : mainReservations) {
                boolean existsInMy = myReservations.stream()
                        .anyMatch(myRes -> myRes.getId().equals(mainRes.getReservationId()));
                if (!existsInMy) {
                    System.out.println("  - 누락된 예약 ID: " + mainRes.getReservationId());
                }
            }

        } catch (Exception e) {
            System.err.println("테이블 비교 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //강제 동기화 실행 (디버깅 로그 포함)
    public void forceSync(String username) {
        System.out.println("=== 강제 동기화 시작 ===");

        // 동기화 전 상태 출력
        compareTables(username);

        // 동기화 실행
        Member member = getCurrentMember(username);
        syncReservationsFromMainSystem(member.getMemberId());

        // 동기화 후 상태 출력
        System.out.println("\n=== 동기화 후 상태 ===");
        compareTables(username);
    }

    //결제 대기 중인 예약 조회 - 개선된 버전
    public List<ReservationMy> getPendingMyReservations(String username) {
        Member member = getCurrentMember(username);

        // 1. 먼저 테이블 간 동기화
        syncReservationsFromMainSystem(member.getMemberId());

        // 2. reservations 테이블에서 결제 대기 중인 예약 조회
        List<ReservationMy> allReservations = reservationMyRepository.findByMemberId(member.getMemberId());

        return allReservations.stream()
                .filter(reservation -> {
                    String status = reservation.getStatus();
                    return "예약완료".equals(status) || "COMPLETED".equals(status) || "PENDING".equals(status);
                })
                .filter(reservation -> !checkPaymentStatus(reservation.getId()))
                .collect(Collectors.toList());
    }

    //메인 시스템(reservation 테이블)과 동기화 - 개선된 버전
    @Transactional
    protected void syncReservationsFromMainSystem(Long memberId) {
        try {
            System.out.println("=== 테이블 간 동기화 시작 ===");
            System.out.println("Member ID: " + memberId);

            // 1. 메인 Reservation 시스템(reservation 테이블)에서 예약 조회
            List<ReservationResponseDto> mainReservations = reservationService.getReservationsByMember(memberId);
            System.out.println("메인 시스템에서 조회된 예약 수: " + mainReservations.size());

            // 2. 각 예약을 ReservationMy(reservations 테이블)에 동기화
            int createdCount = 0;
            int updatedCount = 0;

            for (ReservationResponseDto mainRes : mainReservations) {
                System.out.println("처리 중인 예약 ID: " + mainRes.getReservationId());

                // 3. reservations 테이블에 해당 예약이 있는지 확인
                Optional<ReservationMy> existingReservation = reservationMyRepository.findById(mainRes.getReservationId());

                if (existingReservation.isPresent()) {
                    // 기존 예약이 있으면 상태만 업데이트
                    ReservationMy reservationMy = existingReservation.get();

                    // Payment 상태 확인하여 업데이트
                    boolean hasPayment = checkPaymentStatus(mainRes.getReservationId());
                    String newStatus = hasPayment ? "결제완료" : mainRes.getStatus();

                    if (!newStatus.equals(reservationMy.getStatus())) {
                        reservationMy.setStatus(newStatus);
                        reservationMyRepository.save(reservationMy);
                        updatedCount++;
                        System.out.println("기존 예약 상태 업데이트: " + mainRes.getReservationId() + " -> " + newStatus);
                    }
                } else {
                    // 새 예약 생성하여 reservations 테이블에 삽입
                    boolean hasPayment = checkPaymentStatus(mainRes.getReservationId());
                    String status = hasPayment ? "결제완료" : mainRes.getStatus();

                    ReservationMy newReservationMy = ReservationMy.builder()
                            .id(mainRes.getReservationId())  // 같은 ID 사용
                            .memberId(memberId)
                            .instructorId(mainRes.getInstructorId())
                            .lectureId(mainRes.getLectureId())
                            .date(mainRes.getDate())
                            .status(status)
                            .build();

                    reservationMyRepository.save(newReservationMy);
                    createdCount++;
                    System.out.println("새 예약 추가: " + mainRes.getReservationId() + " -> " + status);
                }
            }

            System.out.println("=== 동기화 완료 ===");
            System.out.println("생성된 예약: " + createdCount + "개, 업데이트된 예약: " + updatedCount + "개");

        } catch (Exception e) {
            System.err.println("테이블 간 동기화 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //특정 예약 ID의 결제 상태 확인 - 성능 개선
    private boolean checkPaymentStatus(Long reservationId) {
        try {
            // 전체 조회 대신 조건부 쿼리로 개선 가능 (나중에 PaymentRepository에 메서드 추가)
            return paymentRepository.findAll().stream()
                    .anyMatch(payment -> payment.getReservation() != null &&
                            payment.getReservation().getReservationId().equals(reservationId) &&
                            "결제완료".equals(payment.getStatus()));
        } catch (Exception e) {
            System.err.println("결제 상태 확인 중 오류: " + e.getMessage());
            return false;
        }
    }

    //결제 완료된 강의 목록 조회 - 테이블 동기화 후
    public List<ReservationMy> getCompletedCourses(String username) {
        Member member = getCurrentMember(username);

        // 1. 먼저 테이블 간 동기화
        syncReservationsFromMainSystem(member.getMemberId());

        // 2. reservations 테이블에서 결제 완료 상태인 예약 조회
        List<ReservationMy> allReservations = reservationMyRepository.findByMemberId(member.getMemberId());

        return allReservations.stream()
                .filter(reservation -> "결제완료".equals(reservation.getStatus()))
                .collect(Collectors.toList());
    }

    //커스텀 결제 완료 조회 메서드 - Payment 엔티티 반환
    public List<Payment> getCompletedPayments(String username) {
        Member member = getCurrentMember(username);

        // 예약 동기화 먼저 실행
        syncReservationsFromMainSystem(member.getMemberId());

        // 전체 조회 후 필터링 (나중에 쿼리 최적화 가능)
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getReservation() != null)
                .filter(payment -> payment.getReservation().getMember() != null)
                .filter(payment -> payment.getReservation().getMember().getMemberId().equals(member.getMemberId()))
                .filter(payment -> "결제완료".equals(payment.getStatus()))
                .collect(Collectors.toList());
    }

    //특정 예약의 결제 여부 확인 (hasPayment와 동일한 기능, 일관성을 위해 유지)
    public boolean hasPayment(Long reservationId) {
        return checkPaymentStatus(reservationId);
    }

    // === 강사 관련 메서드들 ===

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

    // === 게임 정보 관련 메서드들 ===

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

    // === 유틸리티 메서드들 ===

    //상태 매핑 메서드 (현재 미사용이지만 유지)
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
}