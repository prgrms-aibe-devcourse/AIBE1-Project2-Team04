package com.reboot.auth.service;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.dto.SignupDetailsDTO;
import com.reboot.auth.dto.SignupResponse;
import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SignupService {

    private final MemberRepository memberRepository;
    private final InstructorRepository instructorRepository;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    // 기본 프로필 이미지 파일명 설정 (없을 경우 기본값 사용)
    @Value("${default.profile.image:default-profile.png}")
    private String defaultProfileImage;

    public SignupService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.instructorRepository = instructorRepository;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signupProcess(SignupDTO signupDTO) {
        Member member = createMember(signupDTO);
        memberRepository.save(member);
    }

    public void signupDetailsProcess(SignupDetailsDTO signupDetailsDTO) {
        Member member = memberRepository.findByUsername(signupDetailsDTO.username())
                .orElseThrow(() -> new RuntimeException("Member Not Found"));

        Instructor instructor = createInstructor(signupDetailsDTO, member);
        instructorRepository.save(instructor);

        Game game = createGame(signupDetailsDTO, member);
        gameRepository.save(game);
    }

    public boolean existsUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    public SignupResponse validate(SignupDTO dto) {
        if (memberRepository.existsByUsername(dto.username())) {
            return new SignupResponse(false, "username", "이미 사용 중인 아이디입니다.");
        }

        if (memberRepository.existsByEmail(dto.email())) {
            return new SignupResponse(false, "email", "이미 사용 중인 이메일입니다.");
        }

        if (memberRepository.existsByNickname(dto.nickname())) {
            return new SignupResponse(false, "nickname", "이미 사용 중인 닉네임입니다.");
        }

        return new SignupResponse(true, "", "");
    }

    private Member createMember(SignupDTO dto) {
        Member member = new Member();
        member.setUsername(dto.username());
        member.setPassword(passwordEncoder.encode(dto.password()));
        member.setName(dto.name());
        member.setEmail(dto.email());
        member.setNickname(dto.nickname());
        member.setPhone(dto.phone());

        member.setRole("USER");

        // 기본 프로필 이미지 설정
        String defaultImageUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/profiles/" + defaultProfileImage;
        member.setProfileImage(defaultImageUrl);

        return member;
    }

    private Instructor createInstructor(SignupDetailsDTO dto, Member member) {
        Instructor instructor = new Instructor();
        instructor.setMember(member);
        instructor.setCareer(dto.career());
        instructor.setDescription(dto.description());
        instructor.setCreatedAt(LocalDateTime.now());

        member.setInstructor(instructor);
        return instructor;
    }

    private Game createGame(SignupDetailsDTO dto, Member member) {
        Game game = new Game();
        game.setMember(member);
        game.setGameType(dto.gameType());
        game.setGameTier(dto.gameTier());
        game.setGamePosition(dto.gamePosition());

        member.setGame(game);
        return game;
    }

}
