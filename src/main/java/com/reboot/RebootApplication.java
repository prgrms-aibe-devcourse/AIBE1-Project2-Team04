package com.reboot;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.service.MemberService;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.service.LectureService;
import com.reboot.replay.service.ReplayService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@SpringBootApplication
@ComponentScan(
		basePackages = {"com.reboot.reservation", "com.reboot.replay", "com.reboot.lecture", "com.reboot.auth"},
		includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"com.reboot.reservation.*",
				"com.reboot.replay.*",
				"com.reboot.lecture.*",
				"com.reboot.auth.*"
		}),
		excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"com.reboot.common.*",
				"com.reboot.config.*",
				"com.reboot.review.*",
				"com.reboot.survey.*"
		})
)
@EntityScan(basePackages = {
		"com.reboot.reservation.entity",
		"com.reboot.replay.entity",
		"com.reboot.auth.entity",
		"com.reboot.lecture.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.reboot.reservation.repository",
		"com.reboot.replay.repository",
		"com.reboot.lecture.repository",
		"com.reboot.auth.repository"
})
public class RebootApplication {

	public static void main(String[] args) {
		SpringApplication.run(RebootApplication.class, args);
	}
}