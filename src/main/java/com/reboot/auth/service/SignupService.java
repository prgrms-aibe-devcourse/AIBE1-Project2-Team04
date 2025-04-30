package com.reboot.auth.service;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.entity.Student;
import com.reboot.auth.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signupProcess(SignupDTO signupDTO) {
        String userId = signupDTO.userId();
        String password = signupDTO.password();

        boolean isExist = studentRepository.existsByUserid(userId);

        if (isExist) {
            return;
        }

        Student student = new Student();
        student.setUserid(userId);
        student.setPassword(passwordEncoder.encode(password));
        student.setRole("ROLE_ADMIN");
        studentRepository.save(student);
    }
}
