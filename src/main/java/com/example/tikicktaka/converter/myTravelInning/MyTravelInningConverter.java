package com.example.tikicktaka.converter.myTravelInning;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.MyTravelInning;
import com.example.tikicktaka.web.dto.myPage.MyTravelInningRequestDTO;
import com.example.tikicktaka.web.dto.myPage.MyTravelInningResponseDTO;
import com.example.tikicktaka.domain.teams.Team;

public class MyTravelInningConverter {

    public static MyTravelInning toEntity(
            MyTravelInningRequestDTO.Create dto,
            Member member,
            Team preferTeam,
            Team opponentTeam,
            String imageUrl
    ) {
        return MyTravelInning.builder()
                .date(dto.getDate())
                .imageUrl(imageUrl)
                .preferTeam(preferTeam)
                .opponentTeam(opponentTeam)
                .content(dto.getContent())
                .member(member)
                .build();
    }

    public static MyTravelInningResponseDTO.Detail toDetailDTO(MyTravelInning entity) {
        return MyTravelInningResponseDTO.Detail.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .imageUrl(entity.getImageUrl())
                .content(entity.getContent())
                .preferTeamId(entity.getPreferTeam() != null ? entity.getPreferTeam().getId() : null)
                .preferTeamImageUrl(entity.getPreferTeam() != null ? safeLogo(entity.getPreferTeam().getLogoUrl()) : null)
                .opponentTeamId(entity.getOpponentTeam() != null ? entity.getOpponentTeam().getId() : null)
                .opponentTeamImageUrl(entity.getOpponentTeam() != null ? safeLogo(entity.getOpponentTeam().getLogoUrl()) : null)
                .build();
    }

    private static String safeLogo(String url) {
        return (url == null || url.isBlank())
                ? "https://tikicktaka-bucket.s3.ap-northeast-2.amazonaws.com/logo/Companion_Travel.png"
                : url;
    }

    public static MyTravelInningResponseDTO.ListItem toListItemDTO(MyTravelInning entity) {
        return MyTravelInningResponseDTO.ListItem.builder()
                .opponentTeamId(entity.getOpponentTeam().getId())
                .opponentTeamImageUrl(entity.getOpponentTeam().getLogoUrl())
                .imageUrl(entity.getImageUrl()) // 저장 시점에 기본값이 이미 들어갔으니 그냥 반환
                .date(entity.getDate())
                .build();
    }


}
