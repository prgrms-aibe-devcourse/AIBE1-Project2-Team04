package com.reboot.auth.repository;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByMember(Member member);
    Optional<Instructor> findByMemberId(Long memberId);
    boolean existsByMember(Member member);
    boolean existsByMemberId(Long memberId);
}