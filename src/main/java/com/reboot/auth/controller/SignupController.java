package com.reboot.auth.controller;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.service.SignupService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/sign")
    public String signupProcess(SignupDTO signupDTO) {
        if (signupService.signupProcess(signupDTO)) {
            return "success";
        }
        return "fail";
    }
}
