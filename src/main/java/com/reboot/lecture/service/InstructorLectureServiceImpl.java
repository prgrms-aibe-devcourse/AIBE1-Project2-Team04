package com.reboot.lecture.service;

import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.dto.LectureDetailResponseDto;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.entity.LectureDetail;
import com.reboot.lecture.exception.LectureNotFoundException;
import com.reboot.lecture.exception.UnauthorizedLectureAccessException;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.lecture.repository.LectureDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorLectureServiceImpl implements InstructorLectureService {

    private final LectureRepository lectureRepository;
    private final LectureDetailRepository lectureDetailRepository;
    private final GameRepository gameRepository;
    private final InstructorRepository instructorRepository;

    // 특정 강사의 모든 강의 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getLecturesByInstructor(Long instructorId) {
        // 최신순 정렬 추가
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        // 1. 수정된 메소드 이름 사용
        List<Lecture> lectures = lectureRepository.findByInstructorInstructorIdOrderByMetadataCreatedAtDesc(instructorId);

//        // 또는 이미 정렬된 메소드를 사용한다면
//         List<Lecture> lectures = lectureRepository.findByInstructorInstructorIdOrderByCreatedAtDesc(instructorId);


        return lectures.stream()
                .map(LectureResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 강의 상세 정보 조회 (특정 강사의 강의만)
    @Override
    @Transactional(readOnly = true)
    public LectureResponseDto getLectureByIdAndInstructor(String lectureId, Long instructorId) {
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        validateLectureOwnership(lecture, instructorId);

        return LectureResponseDto.fromEntity(lecture);
    }

    // 강의 상세 정보 조회 (LectureDetail)
    @Override
    @Transactional(readOnly = true)
    public LectureDetailResponseDto getLectureDetailByIdAndInstructor(String lectureId, Long instructorId) {
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증
        validateLectureOwnership(lecture, instructorId);

        // LectureDetail 조회
        LectureDetail detail = lectureDetailRepository.findByLectureId(id)
                .orElseThrow(() -> new LectureNotFoundException("강의 상세 정보를 찾을 수 없습니다: " + lectureId));

        return LectureDetailResponseDto.fromEntity(detail);
    }

    // 강의 엔티티 직접 조회 (수정 폼에서 사용)
    @Override
    @Transactional(readOnly = true)
    public Lecture getLectureEntityByIdAndInstructor(String lectureId, Long instructorId) {
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증
        validateLectureOwnership(lecture, instructorId);

        // LectureDetail도 함께 로드 (Lazy Loading 대응)
        if (lecture.getLectureDetail() != null) {
            lecture.getLectureDetail().getOverview(); // 강제 로드
        }

        return lecture;
    }

    // 새 강의 생성
    @Override
    @Transactional
    public LectureResponseDto createLecture(LectureRequestDto request, Instructor instructor) {
        // 1. Lecture 생성 및 저장
        Lecture lecture = request.toEntity();
        lecture.setInstructor(instructor);
        Lecture savedLecture = lectureRepository.save(lecture);

        // 2. LectureDetail 생성 및 저장
        LectureDetail detail = request.toDetailEntity(savedLecture);
        LectureDetail savedDetail = lectureDetailRepository.save(detail);

        return LectureResponseDto.fromEntity(savedLecture);
    }

    // 기존 강의 수정
    @Override
    @Transactional
    public LectureResponseDto updateLecture(String lectureId, LectureRequestDto request, Long instructorId) {
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        validateLectureOwnership(lecture, instructorId);

        // 1. LectureInfo 업데이트
        if (lecture.getInfo() == null) {
            lecture.setInfo(new LectureInfo());
        }

        LectureInfo info = lecture.getInfo();
        info.setTitle(request.getTitle());
        info.setDescription(request.getDescription());
        info.setGameType(request.getGameType());
        info.setPrice(request.getPrice());
        info.setImageUrl(request.getImageUrl());
        info.setDuration(request.getDuration());
        info.setLectureRank(request.getLectureRank());
        info.setPosition(request.getPosition());

        // 2. LectureDetail 업데이트
        LectureDetail detail = lectureDetailRepository.findByLectureId(id)
                .orElseGet(() -> {
                    LectureDetail newDetail = new LectureDetail();
                    newDetail.setLecture(lecture);
                    return newDetail;
                });

        detail.setOverview(request.getOverview());
        detail.setLearningObjectives(request.getLearningObjectives());
        detail.setCurriculum(request.getCurriculum());
        detail.setPrerequisites(request.getPrerequisites());
        detail.setTargetAudience(request.getTargetAudience());
        detail.setInstructorBio(request.getInstructorBio());

        lectureDetailRepository.save(detail);

        Lecture updatedLecture = lectureRepository.save(lecture);

        return LectureResponseDto.fromEntity(updatedLecture);
    }

    // 강의 삭제
    @Override
    @Transactional
    public void deleteLecture(String lectureId, Long instructorId) {
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        validateLectureOwnership(lecture, instructorId);

        // Cascade 설정에 따라 LectureDetail도 자동 삭제됨
        lectureRepository.delete(lecture);
    }

    // 활성화/비활성화 토글 기능
    @Override
    @Transactional
    public LectureResponseDto toggleLectureActive(String lectureId, Long instructorId) {
        Long id = extractLectureId(lectureId);
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        validateLectureOwnership(lecture, instructorId);

        // 토글 로직이 필요한 경우 여기에 구현
        // 현재는 현재 상태 그대로 반환
        return LectureResponseDto.fromEntity(lecture);
    }

    // 강의 소유권 검증
    private void validateLectureOwnership(Lecture lecture, Long instructorId) {
        if (!lecture.getInstructor().getInstructorId().equals(instructorId)) {
            throw new UnauthorizedLectureAccessException("해당 강의에 대한 접근 권한이 없습니다.");
        }
    }

    // lectureId에서 숫자 부분만 추출하는 메서드
    private Long extractLectureId(String lectureId) {
        if (lectureId == null || lectureId.isEmpty()) {
            throw new IllegalArgumentException("강의 ID가 유효하지 않습니다.");
        }

        try {
            if (lectureId.startsWith("LECTURE-")) {
                return Long.parseLong(lectureId.substring("LECTURE-".length()));
            }
            return Long.parseLong(lectureId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("강의 ID 형식이 유효하지 않습니다: " + lectureId);
        }
    }

    // 강사의 게임 정보 가져오기
    @Override
    @Transactional(readOnly = true)
    public Game getInstructorGame(Long instructorId) {
        Instructor instructor = getInstructor(instructorId);
        Long memberId = instructor.getMemberId();

        List<Game> games = gameRepository.findByMember_MemberId(memberId);
        return games.isEmpty() ? null : games.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public String getInstructorGameType(Long instructorId) {
        Game game = getInstructorGame(instructorId);
        return game != null ? game.getGameType() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public String getInstructorGamePosition(Long instructorId) {
        Game game = getInstructorGame(instructorId);
        return game != null ? game.getGamePosition() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public String getInstructorGameTier(Long instructorId) {
        Game game = getInstructorGame(instructorId);
        return game != null ? game.getGameTier() : null;
    }

    // 강사 정보 조회 헬퍼 메서드
    private Instructor getInstructor(Long instructorId) {
        return instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("강사를 찾을 수 없습니다: " + instructorId));
    }
}