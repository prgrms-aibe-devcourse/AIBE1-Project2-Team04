package com.reboot.reservation;

import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.controller.ReservationController;
import com.reboot.reservation.repository.ReservationRepository;
import com.reboot.reservation.service.ReservationService;
import com.reboot.auth.service.MemberService;
import com.reboot.lecture.service.LectureService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

/**
 * Reservation 모듈 테스트를 위한 Test Configuration 클래스
 * auth, lecture 등 다른 모듈의 의존성을 모킹하여 독립적인 테스트 환경을 구성합니다.
 */
@TestConfiguration
@ComponentScan(
        basePackages = "com.reboot.reservation",
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {ReservationService.class, ReservationController.class})
        }
)
public class ReservationTestConfig {

    /**
     * ReservationRepository 빈 모킹
     */
    @Bean
    @Primary
    public ReservationRepository reservationRepository() {
        return Mockito.mock(ReservationRepository.class);
    }

    /**
     * MemberRepository 빈 모킹
     */
    @Bean
    @Primary
    public MemberRepository memberRepository() {
        return Mockito.mock(MemberRepository.class);
    }

    /**
     * InstructorRepository 빈 모킹
     */
    @Bean
    @Primary
    public InstructorRepository instructorRepository() {
        return Mockito.mock(InstructorRepository.class);
    }

    /**
     * LectureRepository 빈 모킹
     */
    @Bean
    @Primary
    public LectureRepository lectureRepository() {
        return Mockito.mock(LectureRepository.class);
    }

    /**
     * MemberService 빈 모킹
     */
    @Bean
    @Primary
    public MemberService memberService() {
        return Mockito.mock(MemberService.class);
    }

    /**
     * LectureService 빈 모킹
     */
    @Bean
    @Primary
    public LectureService lectureService() {
        return Mockito.mock(LectureService.class);
    }

    /**
     * ReplayService 빈 모킹 (추가)
     */
    @Bean
    @Primary
    public ReplayService replayService() {
        return Mockito.mock(ReplayService.class);
    }

    /**
     * ReservationService 빈
     */
    @Bean
    @Primary
    public ReservationService reservationService(ReservationRepository reservationRepository,
                                                 MemberRepository memberRepository,
                                                 InstructorRepository instructorRepository,
                                                 LectureRepository lectureRepository,
                                                 ReplayService replayService) { // 매개변수 추가
        return new ReservationService(
                reservationRepository,
                memberRepository,
                instructorRepository,
                lectureRepository,
                replayService); // 매개변수 추가
    }

    /**
     * ReservationController 빈
     */
    @Bean
    @Primary
    public ReservationController reservationController(ReservationService reservationService,
                                                       LectureService lectureService,
                                                       MemberService memberService,
                                                       ReplayService replayService) { // 매개변수 추가
        return new ReservationController(
                reservationService,
                lectureService,
                memberService,
                replayService); // 매개변수 추가
    }
}