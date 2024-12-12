package com.example.tikicktaka.repository.match;

import com.example.tikicktaka.domain.matches.Match;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    // 특정 날짜에 해당하는 경기 리스트 가져오기
    List<Match> findByMatchDate(LocalDate matchDate);

    // 진행 중인 경기를 가져오는 쿼리 (matchStatus가 true인 경우)
    List<Match> findByMatchStatusTrueAndMatchDateTimeBefore(LocalDate now);

    // matchStatus가 true인 모든 경기를 가져오기
    List<Match> findByMatchStatusTrue();

    // 모든 경기의 상태를 가져오기 (예시: status가 false로 되어 있는 경기들만 가져오기)
    List<Match> findByMatchStatusFalse();
}


