package com.reboot.lecture.entity;

import com.reboot.auth.entity.Instructor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Embedded
    private LectureInfo info;

    @Embedded
    private LectureMetaData metadata;

    // 기본값 및 초기화 - Lecture 클래스의 Builder에 통합
    public static class LectureBuilder {
        private LectureInfo info = new LectureInfo();
        private LectureMetaData metadata = new LectureMetaData();

        // 메타데이터의 기본값을 Builder에서 설정
        public LectureBuilder metadata(LectureMetaData metadata) {
            if (metadata == null) {
                metadata = new LectureMetaData();
                metadata.setAverageRating(0.0f);
                metadata.setTotalMembers(0);
                metadata.setReviewCount(0);
            }
            this.metadata = metadata;
            return this;
        }
    }

    // 엔티티 생성 시 호출, 생성 시간과 수정 시간을 현재로 설정
    @PrePersist
    protected void onCreate() {
        if (metadata != null) {
            metadata.setCreatedAt(LocalDateTime.now());
            metadata.setUpdatedAt(LocalDateTime.now());
        }
    }

    // 엔티티 수정 시 호출, 수정 시간을 현재로 업데이트
    @PreUpdate
    protected void onUpdate() {
        if (metadata != null) {
            metadata.setUpdatedAt(LocalDateTime.now());
        }
    }

    // 커스텀 toString 메서드
    // 순환 참조 방지, instructor의 ID만 포함
    @Override
    public String toString() {
        return "Lecture{" +
                "id='" + id + '\'' +
                ", instructorId='" + (instructor != null ? instructor.getInstructorId() : null) + '\'' +
                ", title='" + (info != null ? info.getTitle() : null) + '\'' +
                ", gameType='" + (info != null ? info.getGameType() : null) + '\'' +
                ", price=" + (info != null ? info.getPrice() : null) +
                ", lectureRank='" + (info != null ? info.getLectureRank() : null) + '\'' +
                ", position='" + (info != null ? info.getPosition() : null) + '\'' +
                ", averageRating=" + (metadata != null ? metadata.getAverageRating() : null) +
                ", totalMembers=" + (metadata != null ? metadata.getTotalMembers() : null) +
                ", reviewCount=" + (metadata != null ? metadata.getReviewCount() : null) +
                '}';
    }
}