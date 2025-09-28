package com.example.tikicktaka.domain.teams;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.images.ProfileImg;
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

    private String shortName;  // 두산

    private String stadiumName;

    @Column(columnDefinition = "TEXT")
    private String teamInfo;

    @Column(columnDefinition = "TEXT")
    private String location;

    @Column(name = "logo_url", length = 512)
    private String logoUrl; // ← 추가

    private static final String DEFAULT_LOGO_URL =
            "https://tikicktaka-bucket.s3.ap-northeast-2.amazonaws.com/logo/Companion_Travel.png";

    public void setLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            this.logoUrl = DEFAULT_LOGO_URL;
        } else {
            this.logoUrl = logoUrl;
        }
    }


    //@OneToOne(mappedBy = "team")
    //private MemberTeam memberTeam;

//    @OneToOne(mappedBy = "team")
//    private TeamImg teamImg;
//
//    public void setTeamImg(TeamImg teamImage) {
//        this.teamImg = teamImage;
//    }
}
