package com.example.tikicktaka.repository.match;

import com.example.tikicktaka.domain.gameSchedule.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {
    List<GameSchedule> findByMatchDate(LocalDate matchDate);

    // 진행 중인 경기를 가져오는 쿼리 (matchStatus가 true인 경우)
    List<GameSchedule> findByMatchStatusTrueAndMatchDateTimeBefore(LocalDate now);

    // matchStatus가 true인 모든 경기를 가져오기
    List<GameSchedule> findByMatchStatusTrue();

    // 모든 경기의 상태를 가져오기 (예시: status가 false로 되어 있는 경기들만 가져오기)
    List<GameSchedule> findByMatchStatusFalse();

    //특정 경기 정보를 가져오기
    GameSchedule findByMatchDateAndHomeTeamAndAwayTeam(LocalDate matchDate, String homeTeam, String awayTeam);
}
