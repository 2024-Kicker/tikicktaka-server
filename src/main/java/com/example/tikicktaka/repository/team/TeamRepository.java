package com.example.tikicktaka.repository.team;

import com.example.tikicktaka.domain.teams.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByShortName(String shortName);
}
