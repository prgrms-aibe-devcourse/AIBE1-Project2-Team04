package com.reboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(
		basePackages = {"com.reboot.lecture", "com.reboot.auth",
				"com.reboot.survey"},
		includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"com.reboot.lecture.*",
				"com.reboot.auth.*",
				"com.reboot.survey.*"
		}),
		excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"com.reboot.replay.*",
				"com.reboot.reservation.*",
				"com.reboot.common.*",
				"com.reboot.config.*",
				"com.reboot.review.*",
				"com.reboot.survey.entity.enums.*"
		})
)
@EntityScan(basePackages = {
		"com.reboot.auth.entity",
		"com.reboot.lecture.entity",
		"com.reboot.survey.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.reboot.lecture.repository",
		"com.reboot.auth.repository",
		"com.reboot.survey.repository"
})
public class RebootApplication {

	public static void main(String[] args) {
		SpringApplication.run(RebootApplication.class, args);
	}
}