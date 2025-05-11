package com.reboot.auth.controller;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.dto.SignupDetailsDTO;
import com.reboot.auth.dto.SignupResponse;
import com.reboot.auth.service.SignupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/sign")
    public ResponseEntity<?> signupProcess(@RequestBody SignupDTO dto) {
        SignupResponse validation = signupService.validate(dto);

        if (!validation.isSuccess()) {
            return ResponseEntity.badRequest().body(validation);
        }

        signupService.signupProcess(dto);
        return ResponseEntity.ok(new SignupResponse(true, "", "회원가입 성공"));
    }

    @PostMapping("/sign_instructor")
    public ResponseEntity<?> signupInstructorProcess(@RequestBody SignupDetailsDTO dto) {
        if (!signupService.existsUsername(dto.username())) {
            return ResponseEntity.badRequest().build();
        }
        
        signupService.signupDetailsProcess(dto);
        return ResponseEntity.ok(new SignupResponse(true, "", "강사 정보 저장 성공"));
    }
}
