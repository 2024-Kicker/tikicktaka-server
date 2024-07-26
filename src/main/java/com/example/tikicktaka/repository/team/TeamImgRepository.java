package com.example.tikicktaka.repository.team;

import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.images.TeamImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamImgRepository extends JpaRepository<TeamImg, Long> {

    Optional<TeamImg> findByTeam_Id(Long id);
}
