package com.example.tikicktaka.domain.images;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.teams.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamImg extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_img_id")
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String logoUrl;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String stadiumUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public void setTeam(Team team) {
        this.team = team;
        team.setTeamImg(this);
    }
}
