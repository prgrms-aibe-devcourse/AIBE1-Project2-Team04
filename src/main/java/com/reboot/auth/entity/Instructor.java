package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "instructor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instructor_id")
    private Long instructorId;

    @OneToOne
    @JoinColumn(name = "instructor_id")
    private Member member;

    @Column(name = "member_id", insertable = false, updatable = false)
    private Long memberId;

    // 강사정보
//    @Column(name = "Nickname", unique = true, nullable = false)
//    private String nickname; // 강사 닉네임 (강의 목록 표시에 필요)
// Member 에서 가져오기로 함..

    @Column(length = 1000)
    private String career;

    @Column(length = 2000)
    private String description;

    @Column(name = "review_num")
    private int reviewNum = 0;

    @Column(name = "average_rating") // rating -> average_rating 수정
    private double averageRating = 0.0;



    // 추가 필드 - InstructorRepository 에서 사용중인 추가 필드

//    @Column(name = "is_active", nullable = false)
//    private Boolean isActive = true; // 강사 활성화 상태 (목록에서 보여줄 때 필요)

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 강사(계정) 등록 시간 -> 신규 강사 표시 및 최신순 정렬 사용

//    @Column(name = "deleted_at")
//    private LocalDateTime deletedAt; // 강사 삭제 시간 (소프트 삭제용)

    @Column(name = "total_members")
    private Integer totalMembers; // 총 수강생 수

//    @Column(name = "expertise_game_types")
//    private String expertiseGameTypes; // 전문 게임 분야
//
//    @Column(name = "expertise_positions")
//    private String expertisePositions; // 전문 포지션



    // ==== 추가 메서드 ====

    // MemberId를 얻기 위한 메서드 (Member 엔티티에서 데이터 가져옴)
    public Long getMemberId() {
        return member != null ? member.getMemberId() : null;
    }

    // Username 을 얻기 위한 메서드 (필요한 경우)
    public String getUsername() {
        return member != null ? member.getUsername() : null;
    }

    // Nickname 을 얻기 위한 메서드 (Member 엔티티에서 데이터 가져옴)
    public String getNickname() {
        return member != null ? member.getNickname() : null;
    }



//    // 소프트 삭제 처리
//    public void delete() {
//        this.deletedAt = LocalDateTime.now();
//        this.isActive = false;
//    }

    // 활성화 상태 토글 메서드 (InstructorLectureServiceImpl 에서 사용)
    public void toggleActive() {
        // Member 활성화 상태 토글 등 필요한 로직
    }





    // ==== Game 정보 접근 메서드 (서비스 레이어에서 구현 - InstructorLectureService) ====

    // 이 메서드들은 실제로 구현되지 않고, 서비스 레이어에서 처리될 예정
    // 여기서는 메서드 시그니처만 제공


    // Member 와 연결된 Game 정보를 가져옵니다.
    // 실제 구현은 서비스 레이어에서 이루어집니다. - InstructorLectureService
    public Game getGame() {
        // 실제 구현은 서비스 레이어에서 수행
        return null;
    }


    // Member 와 연결된 Game 정보에서 게임 타입을 가져옵니다.
    // 실제 구현은 서비스 레이어에서 이루어집니다. - InstructorLectureService
    public String getGameType() {
        // 실제 구현은 서비스 레이어에서 수행
        return null;
    }


    // Member 와 연결된 Game 정보에서 게임 포지션을 가져옵니다.
    // 실제 구현은 서비스 레이어에서 이루어집니다. - InstructorLectureService
    public String getGamePosition() {
        // 실제 구현은 서비스 레이어에서 수행
        return null;
    }


    // Member 와 연결된 Game 정보에서 게임 티어를 가져옵니다.
    // 실제 구현은 서비스 레이어에서 이루어집니다. - InstructorLectureService
    public String getGameTier() {
        // 실제 구현은 서비스 레이어에서 수행
        return null;
    }
}