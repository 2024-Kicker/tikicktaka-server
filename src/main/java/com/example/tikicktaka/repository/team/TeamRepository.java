package com.example.tikicktaka.repository.team;

import com.example.tikicktaka.domain.teams.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
