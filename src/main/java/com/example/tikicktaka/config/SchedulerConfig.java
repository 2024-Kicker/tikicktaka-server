package com.example.tikicktaka.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 동시에 실행할 스레드 수 설정
        scheduler.setThreadNamePrefix("GameDetailScheduler-");
        scheduler.setRemoveOnCancelPolicy(true);              // 취소된 작업 큐에서 제거
        scheduler.setWaitForTasksToCompleteOnShutdown(true);  // 종료 시 대기
        scheduler.setAwaitTerminationSeconds(30);             // 최대 대기 시간
        scheduler.setErrorHandler(t -> {
            System.err.println("Uncaught scheduler error: " + t);
            t.printStackTrace();
        });
        scheduler.initialize();
        return scheduler;
    }
}

