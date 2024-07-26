package com.example.tikicktaka.domain.teams;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.images.TeamImg;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Team extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    private String teamName;

    @Column(columnDefinition = "TEXT")
    private String teamInfo;

    @Column(columnDefinition = "TEXT")
    private String location;

    @OneToOne(mappedBy = "team")
    private MemberTeam memberTeam;

    @OneToOne(mappedBy = "team")
    private TeamImg teamImg;

    public void setTeamImg(TeamImg teamImage) {
        this.teamImg = teamImage;
    }
}
