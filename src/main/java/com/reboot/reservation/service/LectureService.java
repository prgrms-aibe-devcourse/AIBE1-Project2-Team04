package com.reboot.reservation.service;

import com.reboot.reservation.entity.Lecture;
import com.reboot.reservation.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureRepository lectureRepository;

    @Transactional(readOnly = true)
    public Lecture getLecture(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
    }
}