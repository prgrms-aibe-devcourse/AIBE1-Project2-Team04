package com.reboot.auth.repository;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByMember(Member member);
    Optional<Instructor> findByMember_MemberId(Long memberId); //수정
    boolean existsByMember(Member member);
    boolean existsByMember_MemberId(Long memberId); //수정
}