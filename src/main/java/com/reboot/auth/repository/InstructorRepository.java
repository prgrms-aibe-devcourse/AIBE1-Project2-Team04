package com.reboot.auth.repository;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Student, Long> {
    Optional<Instructor> findByEmail(String email);
    Optional<Instructor> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
