package com.reboot.lecture.dto;

import com.reboot.lecture.entity.LectureDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LectureDetailResponseDto {
    private Long lectureId;
    private String overview;
    private String learningObjectives;
    private String prerequisites;
    private String targetAudience;
    private String curriculum;
    private String instructorBio;

    @Builder
    public LectureDetailResponseDto(Long lectureId, String overview, String learningObjectives,
                                    String prerequisites, String targetAudience,
                                    String curriculum, String instructorBio) {
        this.lectureId = lectureId;
        this.overview = overview;
        this.learningObjectives = learningObjectives;
        this.prerequisites = prerequisites;
        this.targetAudience = targetAudience;
        this.curriculum = curriculum;
        this.instructorBio = instructorBio;
    }

    // Entity를 DTO로 변환하는 정적 메서드
    public static LectureDetailResponseDto fromEntity(LectureDetail lectureDetail) {
        return LectureDetailResponseDto.builder()
                .lectureId(lectureDetail.getLecture().getId())
                .overview(lectureDetail.getOverview())
                .learningObjectives(lectureDetail.getLearningObjectives())
                .prerequisites(lectureDetail.getPrerequisites())
                .targetAudience(lectureDetail.getTargetAudience())
                .curriculum(lectureDetail.getCurriculum())
                .instructorBio(lectureDetail.getInstructorBio())
                .build();
    }
}