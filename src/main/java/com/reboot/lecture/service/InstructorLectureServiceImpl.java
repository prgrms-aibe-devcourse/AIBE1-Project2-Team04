package com.reboot.lecture.service;

import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.dto.LectureRequestDto;
import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.entity.LectureMetaData;
import com.reboot.lecture.exception.LectureNotFoundException;
import com.reboot.lecture.exception.UnauthorizedLectureAccessException;
import com.reboot.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


// 강사 전용 강의 관리 서비스 구현체
// 강사가 자신의 강의를 생성, 조회, 수정, 삭제하는 비즈니스 로직 처리
@Service
@RequiredArgsConstructor
public class InstructorLectureServiceImpl implements InstructorLectureService {

    private final LectureRepository lectureRepository;
    private final GameRepository gameRepository; // 추가: Game 레포지토리
    private final InstructorRepository instructorRepository;


    // 특정 강사의 모든 강의 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getLecturesByInstructor(Long instructorId) {
        List<Lecture> lectures = lectureRepository.findByInstructorInstructorId(instructorId);
        return lectures.stream()
                .map(LectureResponseDto::fromEntity)
                .collect(Collectors.toList());
    }


    // 특정 강의 상세 정보 조회 (특정 강사의 강의만)
    // 권한 검증 후 조회
    @Override
    @Transactional(readOnly = true)
    public LectureResponseDto getLectureByIdAndInstructor(String lectureId, Long instructorId) {
        // lectureId에서 프리픽스 제거 및 숫자 부분 추출
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
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
        // lectureId에서 프리픽스 제거 및 숫자 부분 추출
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증: 본인 강의인지 확인
        validateLectureOwnership(lecture, instructorId);

        // 필드 업데이트
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

        // 저장
        Lecture updatedLecture = lectureRepository.save(lecture);

        return LectureResponseDto.fromEntity(updatedLecture);
    }


    // 강의 삭제 (특정 강사의 강의만)
    // 소프트 삭제가 아닌 실제 삭제로 변경
    @Override
    @Transactional
    public void deleteLecture(String lectureId, Long instructorId) {
        // lectureId에서 프리픽스 제거 및 숫자 부분 추출
        Long id = extractLectureId(lectureId);

        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 검증: 본인 강의인지 확인
        validateLectureOwnership(lecture, instructorId);

        // 실제 삭제 처리
        lectureRepository.delete(lecture);
    }


    // 활성화/비활성화 토글 기능 삭제 또는 불필요해짐
    // 필요한 경우 다른 방식으로 대체
    @Override
    @Transactional
    public LectureResponseDto toggleLectureActive(String lectureId, Long instructorId) {
        // 소프트 삭제와 활성화 상태 기능이 제거되었으므로 이 메서드는 더 이상 필요하지 않습니다.
        // 기존 인터페이스 호환성을 위해 남겨두었지만, 실제로는 아무 작업도 수행하지 않습니다.
        // 향후 다른 기능(예: 강의 공개/비공개 설정)으로 대체될 수 있습니다.

        Long id = extractLectureId(lectureId);
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

        validateLectureOwnership(lecture, instructorId);

        // 아무 작업도 수행하지 않고 현재 상태 그대로 반환
        return LectureResponseDto.fromEntity(lecture);
    }


    // 강의 소유권 검증
    // 본인 강의가 아닌 경우 예외 발생
    private void validateLectureOwnership(Lecture lecture, Long instructorId) {
        if (!lecture.getInstructor().getInstructorId().equals(instructorId)) {
            throw new UnauthorizedLectureAccessException("해당 강의에 대한 접근 권한이 없습니다.");
        }
    }

    // lectureId에서 숫자 부분만 추출하는 메서드 - 개선 버전
    private Long extractLectureId(String lectureId) {
        if (lectureId == null || lectureId.isEmpty()) {
            throw new IllegalArgumentException("강의 ID가 유효하지 않습니다.");
        }

        try {
            // "LECTURE-" 프리픽스가 있으면 제거하고 숫자 부분만 추출
            if (lectureId.startsWith("LECTURE-")) {
                return Long.parseLong(lectureId.substring("LECTURE-".length()));
            }
            // 이미 숫자 형식인 경우
            return Long.parseLong(lectureId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("강의 ID 형식이 유효하지 않습니다: " + lectureId);
        }
    }


    // 추가: 강사의 게임 정보 가져오기
    @Override
    @Transactional(readOnly = true)
    public Game getInstructorGame(Long instructorId) {
        // 강사의 멤버 ID 조회
        Instructor instructor = getInstructor(instructorId);
        Long memberId = instructor.getMemberId();

        // 멤버 ID로 게임 정보 조회
        return gameRepository.findByMemberMemberId(memberId)
                .orElse(null); // 게임 정보가 없는 경우 null 반환
    }

    // 추가: 강사의 게임 타입 조회
    @Override
    @Transactional(readOnly = true)
    public String getInstructorGameType(Long instructorId) {
        Game game = getInstructorGame(instructorId);
        return game != null ? game.getGameType() : null;
    }

    // 추가: 강사의 게임 포지션 조회
    @Override
    @Transactional(readOnly = true)
    public String getInstructorGamePosition(Long instructorId) {
        Game game = getInstructorGame(instructorId);
        return game != null ? game.getGamePosition() : null;
    }

    // 추가: 강사의 게임 티어 조회
    @Override
    @Transactional(readOnly = true)
    public String getInstructorGameTier(Long instructorId) {
        Game game = getInstructorGame(instructorId);
        return game != null ? game.getGameTier() : null;
    }

    // 추가: 강사 정보 조회 헬퍼 메서드
    private Instructor getInstructor(Long instructorId) {
        // 강사 정보 조회 로직 (실제 구현은 강사 레포지토리를 사용해야 함)
        // 여기서는 간단히 구현 (실제로는 InstructorRepository 주입 필요)
        // return instructorRepository.findById(instructorId)
        //     .orElseThrow(() -> new RuntimeException("강사를 찾을 수 없습니다: " + instructorId));

        // 임시 구현 (실제 프로젝트에서는 이 부분 수정 필요)
        Lecture lecture = lectureRepository.findFirstByInstructorInstructorId(instructorId)
                .orElseThrow(() -> new RuntimeException("강사를 찾을 수 없습니다: " + instructorId));
        return lecture.getInstructor();
    }

    // 추가: 강의 생성/수정 시 강사의 게임 정보를 활용하는 메서드
    // (필요한 경우 사용)
    public LectureRequestDto enrichLectureWithGameInfo(LectureRequestDto requestDto, Long instructorId) {
        // 이미 설정된 값이 있다면 그대로 사용
        if (requestDto.getGameType() != null && !requestDto.getGameType().isEmpty()) {
            return requestDto;
        }

        // 게임 정보 조회
        Game game = getInstructorGame(instructorId);
        if (game == null) {
            return requestDto;
        }

        // 게임 정보로 DTO 보강
        if (requestDto.getGameType() == null || requestDto.getGameType().isEmpty()) {
            requestDto.setGameType(game.getGameType());
        }

        if (requestDto.getPosition() == null || requestDto.getPosition().isEmpty()) {
            requestDto.setPosition(game.getGamePosition());
        }

        return requestDto;
    }
}