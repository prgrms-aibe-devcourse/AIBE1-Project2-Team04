package com.reboot.lecture.dto;

import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.entity.LectureDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequestDto {
    // 기본 정보 (LectureInfo에 저장)
    private String title;
    private String description;
    private String gameType;
    private Integer price;  // Integer 타입 유지
    private String imageUrl;
    private Integer duration; // 분 단위
    private String lectureRank;
    private String position;

    // 상세 정보 (LectureDetail에 저장)
    private String overview;
    private String learningObjectives;
    private String curriculum;
    private String prerequisites;
    private String targetAudience;
    private String instructorBio;

    // Entity로 변환하는 메서드
    public Lecture toEntity() {
        Lecture lecture = new Lecture();

        // 기본 정보 설정
        LectureInfo info = new LectureInfo();
        info.setTitle(this.title);
        info.setDescription(this.description);
        info.setGameType(this.gameType);
        info.setPrice(this.price);
        info.setImageUrl(this.imageUrl);
        info.setDuration(this.duration);
        info.setLectureRank(this.lectureRank);
        info.setPosition(this.position);

        lecture.setInfo(info);

        return lecture;
    }

    // LectureDetail 생성 메서드
    public LectureDetail toDetailEntity(Lecture lecture) {
        LectureDetail detail = new LectureDetail();
        detail.setLecture(lecture);
        detail.setOverview(this.overview);
        detail.setLearningObjectives(this.learningObjectives);
        detail.setCurriculum(this.curriculum);
        detail.setPrerequisites(this.prerequisites);
        detail.setTargetAudience(this.targetAudience);
        detail.setInstructorBio(this.instructorBio);

        return detail;
    }

    // Response DTO로부터 Request DTO 생성 (detail 정보 분리)
    public static LectureRequestDto fromResponse(LectureResponseDto response, LectureDetailResponseDto detail) {
        LectureRequestDtoBuilder builder = LectureRequestDto.builder()
                .title(response.getTitle())
                .description(response.getDescription())
                .gameType(response.getGameType())
                .price(response.getPrice())  // Integer 그대로 사용
                .imageUrl(response.getImageUrl())
                .duration(response.getDuration())
                .lectureRank(response.getLectureRank())
                .position(response.getPosition());

        // detail이 있는 경우에만 추가
        if (detail != null) {
            builder.overview(detail.getOverview())
                    .learningObjectives(detail.getLearningObjectives())
                    .curriculum(detail.getCurriculum())
                    .prerequisites(detail.getPrerequisites())
                    .targetAudience(detail.getTargetAudience())
                    .instructorBio(detail.getInstructorBio());
        }

        return builder.build();
    }

    // Lecture 엔티티로부터 직접 생성하는 메서드
    public static LectureRequestDto fromEntity(Lecture lecture) {
        LectureInfo info = lecture.getInfo();
        LectureDetail detail = lecture.getLectureDetail();

        LectureRequestDtoBuilder builder = LectureRequestDto.builder()
                .title(info.getTitle())
                .description(info.getDescription())
                .gameType(info.getGameType())
                .price(info.getPrice())  // Integer 그대로 사용
                .imageUrl(info.getImageUrl())
                .duration(info.getDuration())
                .lectureRank(info.getLectureRank())
                .position(info.getPosition());

        if (detail != null) {
            builder.overview(detail.getOverview())
                    .learningObjectives(detail.getLearningObjectives())
                    .curriculum(detail.getCurriculum())
                    .prerequisites(detail.getPrerequisites())
                    .targetAudience(detail.getTargetAudience())
                    .instructorBio(detail.getInstructorBio());
        }

        return builder.build();
    }
}