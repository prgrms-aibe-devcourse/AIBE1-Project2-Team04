package com.reboot.auth.repository;

import com.reboot.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Student, Long> {

    boolean existsByUsername(String username);

    Student findByUsername(String username);
}
