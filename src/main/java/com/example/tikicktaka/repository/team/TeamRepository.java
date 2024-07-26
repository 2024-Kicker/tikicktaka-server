package com.example.tikicktaka.repository.team;

import com.example.tikicktaka.domain.teams.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
