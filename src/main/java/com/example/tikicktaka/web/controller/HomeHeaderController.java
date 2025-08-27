package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.web.dto.home.HomeHeaderResponseDTO;
import com.example.tikicktaka.service.homeService.HomeHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeHeaderController {

    private final HomeHeaderService service;

    // GET /api/home/header?teamId=3
    @GetMapping("/header")
    public HomeHeaderResponseDTO getHeader(@RequestParam(required = false) Long teamId) {
        return service.getHomeHeader(teamId);
    }
}

