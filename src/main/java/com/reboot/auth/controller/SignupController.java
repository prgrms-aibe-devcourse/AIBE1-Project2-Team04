/*
package com.reboot.auth.controller;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.service.SignupService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/sign")
    public String signupProcess(SignupDTO signupDTO) {
        if (signupService.signupProcess(signupDTO)) {
            return "auth/login";
        }
        return "redirect:/sign";
    }
}
*/
