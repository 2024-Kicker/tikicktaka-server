package com.example.tikicktaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TikickTakaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TikickTakaApplication.class, args);
    }
}
