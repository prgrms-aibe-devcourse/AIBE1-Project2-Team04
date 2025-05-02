package com.reboot.auth.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
public class TestController {

    @GetMapping("/")
    public String index() {
        String userInfo = SecurityContextHolder.getContext().toString();
        return userInfo;
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
