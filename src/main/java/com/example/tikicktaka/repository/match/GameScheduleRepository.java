package com.example.tikicktaka.repository.match;

import com.example.tikicktaka.domain.gameSchedule.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface  GameScheduleRepository extends JpaRepository<GameSchedule, Long> {
    List<GameSchedule> findByMatchDate(LocalDate matchDate);

    // 진행 중인 경기를 가져오는 쿼리 (matchStatus가 true인 경우)
    List<GameSchedule> findByMatchStatusTrueAndMatchDateTimeBefore(LocalDate now);

    // matchStatus가 true인 모든 경기를 가져오기
    List<GameSchedule> findByMatchStatusTrue();

    // 모든 경기의 상태를 가져오기 (예시: status가 false로 되어 있는 경기들만 가져오기)
    List<GameSchedule> findByMatchStatusFalse();

    //특정 경기 정보를 가져오기
    GameSchedule findByMatchDateAndHomeTeamAndAwayTeam(LocalDate matchDate, String homeTeam, String awayTeam);

    Optional<GameSchedule> findFirstByHomeTeamOrAwayTeamAndMatchDateGreaterThanEqualOrderByMatchDateAsc(
            String homeTeam, String awayTeam, LocalDate matchDate
    );
    // 기준팀이 "홈"인 가장 가까운 경기 1건
    Optional<GameSchedule> findFirstByHomeTeamAndMatchDateTimeGreaterThanEqualOrderByMatchDateTimeAsc(
            String homeTeam, LocalDateTime from
    );

    // 기준팀이 "원정"인 가장 가까운 경기 1건
    Optional<GameSchedule> findFirstByAwayTeamAndMatchDateTimeGreaterThanEqualOrderByMatchDateTimeAsc(
            String awayTeam, LocalDateTime from
    );
}

