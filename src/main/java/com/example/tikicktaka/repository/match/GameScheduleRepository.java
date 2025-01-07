package com.example.tikicktaka.repository.match;

import com.example.tikicktaka.domain.matches.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {
}
