package com.reboot.auth.service;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.entity.Student;
import com.reboot.auth.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    private final MemberRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(MemberRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signupProcess(SignupDTO signupDTO) {
        String username = signupDTO.username();
        String password = signupDTO.password();

        boolean isExist = studentRepository.existsByUsername(username);

        if (isExist) {
            return;
        }

        Student student = new Student();
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode(password));
        student.setRole("ADMIN");
        studentRepository.save(student);
    }
}
