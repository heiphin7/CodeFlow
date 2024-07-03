package com.codeflow.controller;

import com.codeflow.dto.AuthenticationDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthenticationController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("AuthenticationDto", new AuthenticationDto());
        return "login";
    }

    @GetMapping("/main")
    public String mainPage() {
        return "index";
    }
}
