package com.example.tikicktaka.service.teamService;

import com.example.tikicktaka.domain.teams.Team;

import java.util.List;
import java.util.Optional;

public interface TeamQueryService {

    Optional<Team> findTeamById(Long teamId);

    List<Team> getTeamList();
}
