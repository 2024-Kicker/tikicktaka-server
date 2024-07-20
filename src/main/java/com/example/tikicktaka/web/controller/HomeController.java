package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.service.memberService.CustomOAuth2UserService;
import com.example.tikicktaka.domain.member.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    public HomeController(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @GetMapping("/")
    public String home(Model model) {
        User currentUser = customOAuth2UserService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "index";
    }
}
