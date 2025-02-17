package com.example.tikicktaka.domain.mapping.member;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.domain.travel.TravelStyle;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberTravelStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member; // 한 명당 하나의 여행 스타일 데이터만 존재

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_one", nullable = false)
    private TravelStyle styleOne; // 첫 번째 선호 여행 스타일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_two", nullable = false)
    private TravelStyle styleTwo; // 두 번째 선호 여행 스타일

    /**
     * 여행 스타일 업데이트 메서드
     */
    public void updateStyles(TravelStyle StyleOne, TravelStyle StyleTwo) {
        this.styleOne = StyleOne;
        this.styleTwo = StyleTwo;
    }
}

