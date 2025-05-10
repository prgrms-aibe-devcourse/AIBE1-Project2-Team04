package com.reboot.auth.service;

import com.reboot.auth.dto.ProfileDTO;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.ReservationMyRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MypageService {

    private final MemberRepository memberRepository;
    private final ReservationMyRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

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
            String imageUrl = fileUploadService.uploadImageToSupabase(profileImage);
            member.setProfileImage(imageUrl);
        }
        // 저장
        memberRepository.save(member);
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
}
