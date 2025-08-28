package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.web.dto.home.HomeHeaderResponseDTO;
import com.example.tikicktaka.service.homeService.HomeHeaderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Home Screen", description = "홈 화면 출력 api")
@RequestMapping("/api/home")
public class HomeHeaderController {

    private final HomeHeaderService service;

    // GET /api/home/header?teamId=3
    @GetMapping("/header")
    public HomeHeaderResponseDTO getHeader(@RequestParam(required = false) Long teamId) {
        return service.getHomeHeader(teamId);
    }
}

