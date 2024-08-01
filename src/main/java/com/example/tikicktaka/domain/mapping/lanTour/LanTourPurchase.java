package com.example.tikicktaka.domain.mapping.lanTour;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LanTourPurchase extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lan_tour_purchase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lan_tour_id")
    private LanTour lanTour;

    public void setMember(Member member) {
        if (this.member != null)
            member.getLanTourPurchaseList().remove(this);
        this.member = member;
        member.getLanTourPurchaseList().add(this);
    }

    public void setLanTour(LanTour lanTour) {
        if (this.lanTour != null)
            lanTour.getLanTourPurchaseList().remove(this);
        this.lanTour = lanTour;
        lanTour.getLanTourPurchaseList().add(this);
    }
}
