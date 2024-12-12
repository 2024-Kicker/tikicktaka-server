package com.example.tikicktaka.service.matchService;

import com.example.tikicktaka.repository.match.MatchRepository;
import com.example.tikicktaka.domain.matches.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DataService {

    @Autowired
    private MatchRepository matchRepository;

    public void saveMatches(List<Map<String, Object>> dataList) {
        for (Map<String, Object> data : dataList) {
            Match match = new Match();
            match.setMatchDate(LocalDate.parse((String) data.get("matchDate")));
            //match.setMatchTime(LocalDateTime.parse((String) data.get("matchTime")));

            //match.setMatchDate((String) data.get("matchDate"));
            match.setMatchDateTime(LocalDateTime.parse((String) data.get("matchTime")));
            match.setHomeTeam((String) data.get("homeTeam"));
            match.setAwayTeam((String) data.get("awayTeam"));
            match.setScore((String) data.get("score"));
            match.setMatchField((String)data.get("matchField"));
            matchRepository.save(match);
        }
    }
}

