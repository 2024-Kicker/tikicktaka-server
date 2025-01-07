//package com.example.tikicktaka.web.controller;
//
//import com.example.tikicktaka.service.matchService.CrawlerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/crawl")
//public class CrawlerController {
//
//    @Autowired
//    private CrawlerService crawlerService;
//
//    // API 호출 시 크롤링 동작
//    @GetMapping("/matches")
//    public String crawlMatches() {
//        // 크롤링 서비스 실행
//        crawlerService.crawlData();
//        return "Data crawled and saved to database!";
//    }
//}
//
