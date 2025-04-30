package com.reboot.auth.controller;

import com.reboot.auth.dto.SignupDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class SignupController {

    @PostMapping("/sign")
    public String signupProcess(SignupDTO signupDTO) {
        return "ok";
    }
}
