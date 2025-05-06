package com.reboot.replay;

import com.reboot.replay.repository.ReplayRepository;
import com.reboot.replay.service.ReplayService;
import com.reboot.replay.service.ReplayServiceImpl;
import com.reboot.reservation.repository.ReservationRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Replay 모듈 테스트를 위한 설정 클래스
 * 다른 모듈의 의존성을 모킹하기 위한 설정을 제공합니다.
 */
@Configuration
@Profile("test")
public class ReplayTestConfig {

    @Bean
    @Primary
    public ReplayRepository replayRepository() {
        return Mockito.mock(ReplayRepository.class);
    }

    @Bean
    @Primary
    public ReservationRepository reservationRepository() {
        return Mockito.mock(ReservationRepository.class);
    }

    @Bean
    public ReplayService replayService() {
        return new ReplayServiceImpl(replayRepository(), reservationRepository());
    }
}
