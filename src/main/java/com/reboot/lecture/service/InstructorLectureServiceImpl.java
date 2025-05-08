package com.reboot.lecture.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.exception.LectureNotFoundException;
import com.reboot.lecture.exception.UnauthorizedLectureAccessException;
import com.reboot.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


// 강사 전용 강의 관리 서비스 구현체
// 강사가 자신의 강의를 생성, 조회, 수정, 삭제하는 비즈니스 로직 처리
@Service
@RequiredArgsConstructor
public class InstructorLectureServiceImpl implements InstructorLectureService {

    private final LectureRepository lectureRepository;


    // 특정 강사의 모든 강의 목록 조회
    // 삭제되지 않은 모든 강의 포함 (활성/비활성 모두)
    @Override
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getLecturesByInstructor(Long instructorId) {

        List<Lecture> lectures = lectureRepository.findByInstructorInstructorIdAndDeletedAtIsNull(instructorId);
        return lectures.stream()
                .map(LectureResponseDto::fromEntity)
                .collect(Collectors.toList());
    }


    // 특정 강의 상세 정보 조회 (특정 강사의 강의만)
    // 권한 검증 후 조회
    @Override
    @Transactional(readOnly = true)
    public LectureResponseDto getLectureByIdAndInstructor(String lectureId, Long instructorId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증: 본인 강의인지 확인
        validateLectureOwnership(lecture, instructorId);

        return LectureResponseDto.fromEntity(lecture);
    }


    // 새 강의 생성
    @Override
    @Transactional
    public LectureResponseDto createLecture(LectureRequestDto request, Instructor instructor) {
        Lecture lecture = request.toEntity();

        // 강사 정보 설정
        lecture.setInstructor(instructor);

        // 저장
        Lecture savedLecture = lectureRepository.save(lecture);

        return LectureResponseDto.fromEntity(savedLecture);
    }


    // 기존 강의 수정 (특정 강사의 강의만)
    // 권한 검증 후 수정
    @Override
    @Transactional
    public LectureResponseDto updateLecture(String lectureId, LectureRequestDto request, Long instructorId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증: 본인 강의인지 확인
        validateLectureOwnership(lecture, instructorId);

        // 필드 업데이트
        lecture.setTitle(request.getTitle());
        lecture.setDescription(request.getDescription());
        lecture.setGameType(request.getGameType());
        lecture.setPrice(request.getPrice());
        lecture.setImageUrl(request.getImageUrl());
        lecture.setDuration(request.getDuration());
        lecture.setLectureRank(request.getLectureRank());
        lecture.setPosition(request.getPosition());

        // 저장
        Lecture updatedLecture = lectureRepository.save(lecture);

        return LectureResponseDto.fromEntity(updatedLecture);
    }


    // 강의 삭제 (특정 강사의 강의만) - 소프트 삭제 처리
    // 권한 검증 후 삭제
    @Override
    @Transactional
    public void deleteLecture(String lectureId, Long instructorId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증: 본인 강의인지 확인
        validateLectureOwnership(lecture, instructorId);

        // 소프트 삭제 처리
        lecture.setDeletedAt(LocalDateTime.now());
        lecture.setIsActive(false);

        lectureRepository.save(lecture);
    }


    // 강의 활성화/비활성화 토글 (특정 강사의 강의만)
    // 권한 검증 후 토글
    @Override
    @Transactional
    public LectureResponseDto toggleLectureActive(String lectureId, Long instructorId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증: 본인 강의인지 확인
        validateLectureOwnership(lecture, instructorId);

        // 활성화 상태 토글
        lecture.setIsActive(!lecture.getIsActive());

        // 저장
        Lecture updatedLecture = lectureRepository.save(lecture);

        return LectureResponseDto.fromEntity(updatedLecture);
    }


    // 강의 소유권 검증
    // 본인 강의가 아닌 경우 예외 발생
    private void validateLectureOwnership(Lecture lecture, Long instructorId) {
        if (!lecture.getInstructor().getInstructorId().equals(instructorId)) {
            throw new UnauthorizedLectureAccessException("해당 강의에 대한 접근 권한이 없습니다.");
        }
    }
}