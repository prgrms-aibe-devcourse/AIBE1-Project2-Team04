package com.reboot.auth.service;

import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.dto.GameDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.*;
import com.reboot.payment.entity.Payment;
import com.reboot.payment.repository.PaymentRepository;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MypageService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // 생성자 수정 - 모든 필수 의존성 포함
    public MypageService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         FileUploadService fileUploadService,
                         PaymentRepository paymentRepository,
                         InstructorRepository instructorRepository,
                         GameRepository gameRepository,
                         ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
        this.paymentRepository = paymentRepository;
        this.instructorRepository = instructorRepository;
        this.gameRepository = gameRepository;
        this.reservationRepository = reservationRepository;
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

    // === 예약 및 결제 관련 메서드들 (단순화) ===

    /**
     * 결제 대기 중인 예약 조회 (단순 버전)
     */
    public List<Reservation> getPendingReservations(String username) {
        Member member = getCurrentMember(username);

        // 해당 회원의 모든 예약 조회
        List<Reservation> allReservations = reservationRepository.findByMember_MemberId(member.getMemberId());

        // 결제 대기 중인 예약만 필터링 (예약완료 상태이면서 결제가 안된 것)
        return allReservations.stream()
                .filter(reservation -> "예약완료".equals(reservation.getStatus()))
                .filter(reservation -> !hasPayment(reservation.getReservationId()))
                .collect(Collectors.toList());
    }

    /**
     * 결제 완료된 Payment 목록 조회 (단순 버전) - COMPLETED 상태 사용
     */
    public List<Payment> getCompletedPayments(String username) {
        Member member = getCurrentMember(username);

        // PaymentService에서 사용하는 상태값에 맞춤
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getReservation() != null &&
                        payment.getReservation().getMember().getMemberId().equals(member.getMemberId()) &&
                        ("결제완료".equals(payment.getStatus()) || "COMPLETED".equals(payment.getStatus())))  // 두 값 모두 지원
                .collect(Collectors.toList());
    }

    /**
     * 결제 완료된 예약 목록 조회 (단순 버전)
     */
    public List<Reservation> getCompletedReservations(String username) {
        Member member = getCurrentMember(username);

        // 해당 회원의 모든 예약 조회
        List<Reservation> allReservations = reservationRepository.findByMember_MemberId(member.getMemberId());

        // 결제 완료된 예약만 필터링
        return allReservations.stream()
                .filter(reservation -> hasPayment(reservation.getReservationId()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 예약의 결제 여부 확인 (단순 버전) - 여러 상태값 지원
     */
    public boolean hasPayment(Long reservationId) {
        return paymentRepository.findAll().stream()
                .anyMatch(payment -> payment.getReservation() != null &&
                        payment.getReservation().getReservationId().equals(reservationId) &&
                        ("결제완료".equals(payment.getStatus()) || "COMPLETED".equals(payment.getStatus())));  // 두 값 모두 지원
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
}