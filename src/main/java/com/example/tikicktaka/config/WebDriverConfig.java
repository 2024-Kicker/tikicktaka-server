package com.example.tikicktaka.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverConfig {

    public static WebDriver createWebDriver() {
        // WebDriverManager를 사용하여 크롬 드라이버 자동 설정
        WebDriverManager.chromedriver().setup();

        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // 예: 헤드리스 모드

        // ChromeDriver 인스턴스 생성
        return new ChromeDriver(options);
    }
}

