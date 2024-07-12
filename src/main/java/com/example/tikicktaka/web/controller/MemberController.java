package com.example.tikicktaka.web.controller;

import io.swagger.annotations.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


public class MemberController {

    @ResponseBody
    @GetMapping("/kakao/back")
    public void kakaoCallback(@RequestParam String code) {
        System.out.println(code);

    }


}
