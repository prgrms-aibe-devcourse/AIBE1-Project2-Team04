package com.reboot.survey.repository;

import com.reboot.auth.entity.Member;
import com.reboot.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByMemberOrderByIdDesc(Member member);
}