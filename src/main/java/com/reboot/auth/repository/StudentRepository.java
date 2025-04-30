package com.reboot.auth.repository;

import com.reboot.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
