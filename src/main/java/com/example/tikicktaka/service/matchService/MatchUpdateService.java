package com.example.tikicktaka.service.matchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.tikicktaka.domain.matches.Match;
import com.example.tikicktaka.repository.match.MatchRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@EnableScheduling
public class MatchUpdateService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CrawlerService crawlerService;

    // 매일 밤 12시 30분에 경기 업데이트
    @Scheduled(cron = "0 16 17 * * *")
    public void updateMatchesDaily() {
        LocalDate today = LocalDate.now();

        // 모든 경기를 가져와 matchStatus를 false로 초기화
        List<Match> allMatches = matchRepository.findAll();
        for (Match match : allMatches) {
            match.setMatchStatus(false);  // 모든 경기 상태를 false로 초기화
            matchRepository.save(match); //이거 저장해주는 코드 까먹어서 디비에 초기화가 반영이 안됨

        }
        System.out.println("초기화 완료");

        // 오늘 경기만 matchStatus를 true로 설정하고, 크롤링해서 matchTime 업데이트
        List<Match> todayMatches = matchRepository.findByMatchDate(today);
        for (Match match : todayMatches) {
            match.setMatchStatus(true);  // 오늘 경기로 상태 변경

            // 경기 시간 크롤링
            String matchTime = crawlerService.crawlMatchTime(match.getHomeTeam(), match.getAwayTeam());
            if (matchTime != null) {
                // 크롤링된 경기 시간을 LocalTime으로 변환
                LocalTime time = LocalTime.parse(matchTime, DateTimeFormatter.ofPattern("HH:mm"));

                // 오늘 날짜와 크롤링된 시간을 결합해 LocalDateTime 생성
                //LocalDateTime matchDateTime = LocalDateTime.of(today, time);
                LocalDateTime matchDateTime = LocalDateTime.of(today, time.withSecond(0).withNano(0));
                // 경기 시간 설정
                match.setMatchDateTime(matchDateTime);

            }
            System.out.println("상태 변경 완료");
            // 변경된 내용을 DB에 저장
            matchRepository.save(match);
        }
    }
}
