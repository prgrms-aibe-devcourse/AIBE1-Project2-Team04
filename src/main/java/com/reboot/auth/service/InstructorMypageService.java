package com.reboot.auth.service;

import com.reboot.auth.dto.InstructorProfileDTO;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InstructorMypageService {

    private final InstructorRepository instructorRepository;
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final FileUploadService fileUploadService;

    public InstructorMypageService(InstructorRepository instructorRepository,
                                   MemberRepository memberRepository,
                                   GameRepository gameRepository,
                                   FileUploadService fileUploadService) {
        this.instructorRepository = instructorRepository;
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
        this.fileUploadService = fileUploadService;
    }

    public boolean isInstructor(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return instructorRepository.existsByMember(member);
    }

    public Instructor getInstructor(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return instructorRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("강사 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateInstructorProfile(String username, InstructorProfileDTO dto, MultipartFile profileImage) throws IOException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Instructor instructor = member.getInstructor();
        Game game = member.getGame();

        // Member 정보 수정 (변경 가능한 필드만)
        member.setNickname(dto.getNickname());
        member.setPhone(dto.getPhone());

        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            // 이미지 업로드 로직
            validateProfileImage(profileImage);
            String imageUrl = fileUploadService.uploadImageToSupabase(profileImage);
            if (imageUrl != null) {
                member.setProfileImage(imageUrl);
            }
        }

        // Instructor 정보 수정
        if (instructor != null) {
            instructor.setCareer(dto.getCareer());
            instructor.setDescription(dto.getDescription());
        }

        // Game 정보 수정
        if (game == null) {
            game = new Game();
            game.setMember(member);
        }
        game.setGameType(dto.getGameType());
        game.setGameTier(dto.getGameTier());
        game.setGamePosition(dto.getGamePosition());

        memberRepository.save(member);
        gameRepository.save(game);
    }

    // 현재 상태 조회
    public InstructorProfileDTO getCurrentInstructorProfile(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Instructor instructor = member.getInstructor();
        Game game = member.getGame();

        // 디버깅 로그 추가
        System.out.println("=== 서비스 디버깅 ===");
        System.out.println("username: " + username);
        System.out.println("member: " + member);
        System.out.println("member.nickname: " + (member != null ? member.getNickname() : "null"));
        System.out.println("instructor: " + instructor);
        System.out.println("game: " + game);

        return InstructorProfileDTO.builder()
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .career(instructor != null ? instructor.getCareer() : "")
                .description(instructor != null ? instructor.getDescription() : "")
                .gameType(game != null ? game.getGameType() : "")
                .gameTier(game != null ? game.getGameTier() : "")
                .gamePosition(game != null ? game.getGamePosition() : "")
                .build();
    }

    // 프로필 이미지 유효성 검사 (MypageService에서 복사)
    private void validateProfileImage(MultipartFile file) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif"))) {
            throw new IllegalArgumentException("JPG, PNG, GIF 형식의 이미지만 허용됩니다.");
        }
    }
}
