package com.reboot.auth.service;

import com.reboot.auth.entity.Student;
import com.reboot.auth.repository.MemberRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository studentRepository;

    public CustomUserDetailsService(MemberRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Student student = studentRepository.findByUsername(username);

        if(student != null) {
            return User.builder()
                    .username(student.getUsername())
                    .password(student.getPassword())
                    .roles(student.getRole())
                    .build();
        }

        return null;
    }
}
