package com.example.tikicktaka.service.matchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.tikicktaka.domain.matches.Match;
import com.example.tikicktaka.repository.match.MatchRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class MatchDetailService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CrawlerService crawlerService;

    // 15분마다 경기 세부 내용 업데이트
    //@Scheduled(fixedRate = 15 * 60 * 1000) // 15분 간격
    @Scheduled(cron = "0 56 20 * * *")
    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void updateMatchDetails() {
        LocalDateTime now = LocalDateTime.now();

        // 진행 중인 경기 (matchStatus가 true이고, 현재 시간 이전에 시작한 경기)를 가져오기
        List<Match> ongoingMatches = matchRepository.findByMatchStatusTrue();
        for (Match match : ongoingMatches) {
            LocalDateTime matchStartTime = match.getMatchDateTime();
 
            // 현재 시간이 경기 시작 시간보다 크고, 경기 종료 시간(시작 시간 + 120분)보다 작으면 경기 진행 중
            if (now.isAfter(matchStartTime) && now.isBefore(matchStartTime.plusMinutes(480))) {
                // 경기 세부 내용 크롤링
                CrawlerService.MatchDetails matchDetails = crawlerService.crawlMatchDetails(match.getHomeTeam(), match.getAwayTeam());

                // 크롤링된 세부 내용 출력 (DB에 저장할 필요가 없다면 로그로 출력)
                System.out.println("경기 진행 중: " + match.getHomeTeam() + " vs " + match.getAwayTeam());
                System.out.println("경기 시작 시간: " + match.getMatchDateTime());
                System.out.println("경기장: " + match.getMatchField());
                System.out.println("현재 스코어: " + matchDetails.getScore());
                System.out.println("전반/후반 상태: " + matchDetails.getHalfStatus());
                System.out.println("live 상태: " + matchDetails.getLiveStatus());

            }
        }
    }
}
