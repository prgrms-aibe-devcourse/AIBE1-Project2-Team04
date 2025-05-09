package com.reboot.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "/auth/login";
    }

    @GetMapping("/select_signup_type")
    public String selectType() {
        return "auth/select-signup-type";
    }

    @GetMapping("/sign")
    public String signup(@RequestParam(value = "type", required = false) String type, Model model) {
        model.addAttribute("signupType", type);
        return "auth/signup";
    }

    @GetMapping("/sign_details")
    public String signupDetails() {
        return "auth/signup-instructor-details";
    }
}
