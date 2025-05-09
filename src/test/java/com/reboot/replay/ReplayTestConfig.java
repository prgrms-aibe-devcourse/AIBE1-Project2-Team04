package com.reboot.replay;

import com.reboot.replay.controller.ReplayController;
import com.reboot.replay.repository.ReplayRepository;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.repository.ReservationRepository;
import com.reboot.reservation.service.ReservationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

/**
 * Replay 모듈 테스트를 위한 Test Configuration 클래스
 * auth, common, config 등 다른 모듈의 의존성을 모킹하여 독립적인 테스트 환경을 구성합니다.
 */
@TestConfiguration
@ComponentScan(
        basePackages = "com.reboot.replay",
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {ReplayService.class, ReplayController.class})
        }
)
public class ReplayTestConfig {

    /**
     * ReplayRepository 빈 모킹
     */
    @Bean
    @Primary
    public ReplayRepository replayRepository() {
        return Mockito.mock(ReplayRepository.class);
    }

    /**
     * ReservationRepository 빈 모킹
     */
    @Bean
    @Primary
    public ReservationRepository reservationRepository() {
        return Mockito.mock(ReservationRepository.class);
    }

    /**
     * ReservationService 빈 모킹
     */
    @Bean
    @Primary
    public ReservationService reservationService() {
        return Mockito.mock(ReservationService.class);
    }

    /**
     * ReplayService 빈
     */
    @Bean
    @Primary
    public ReplayService replayService(ReplayRepository replayRepository,
                                       ReservationRepository reservationRepository) {
        return new ReplayService(replayRepository, reservationRepository);
    }

    /**
     * ReplayController 빈
     */
    @Bean
    @Primary
    public ReplayController replayController(ReplayService replayService,
                                             ReservationService reservationService) {
        return new ReplayController(replayService, reservationService);
    }
}