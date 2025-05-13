// import 섹션에 Game 엔티티 import 추가가 필요함
package com.reboot.auth.service;

import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.dto.GameDTO; // GameDTO import 추가
import com.reboot.auth.entity.Game; // Game 엔티티 import 추가
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.GameRepository; // GameRepository import 추가
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.ReservationMyRepository;
import com.reboot.payment.entity.Payment;
import com.reboot.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    private GameRepository gameRepository; // GameRepository 추가

    public MypageService(MemberRepository memberRepository,
                         ReservationMyRepository reservationRepository,
                         PasswordEncoder passwordEncoder,
                         FileUploadService fileUploadService) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
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

    // 결제 완료된 강의 목록 조회
    public List<Payment> getCompletedPayments(String username) {
        Member member = getCurrentMember(username);
        return paymentRepository.findCompletedPaymentsByMemberId(member.getMemberId());
    }

    // 강사 인증 확인
    public boolean isInstructor(String username) {
        Member member = getCurrentMember(username);
//        return "INSTRUCTOR".equals(member.getRole());
        return instructorRepository.existsByMember(member);
    }

    public Instructor getInstructorByMember(String username) {
        Member member = getCurrentMember(username);
        return instructorRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("강사 정보를 찾을 수 없습니다."));
    }

    /**
     * 사용자의 게임 정보 존재 여부 확인
     * @param username 사용자명
     * @return 게임 정보가 있으면 true, 없으면 false
     */
    public boolean hasGameInfo(String username) {
        Member member = getCurrentMember(username);
        return gameRepository.existsByMemberMemberId(member.getMemberId());
    }

    /**
     * 사용자의 게임 정보 저장
     * @param username 사용자명
     * @param gameDTO 게임 정보 DTO
     */
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

    /**
     * 사용자의 현재 게임 정보 조회
     * @param username 사용자명
     * @return 게임 정보
     */
    public Game getCurrentGameByMember(String username) {
        Member member = getCurrentMember(username);

        List<Game> games = gameRepository.findByMember_MemberId(member.getMemberId());
        if (games.isEmpty()) {
            throw new RuntimeException("게임 정보를 찾을 수 없습니다.");
        }
        return games.get(0); // 첫 번째 게임 정보 반환
    }

    /**
     * 사용자의 게임 정보 업데이트
     * @param username 사용자명
     * @param gameDTO 업데이트할 게임 정보
     */
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