package com.example.tikicktaka.domain.mapping.member;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChargeCoin extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_coin_id")
    private Long id;

    private Long amount;

    private Long leftover;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void setMember(Member member) {
        if (this.member != null)
            member.getChargeCoinList().remove(this);
        this.member = member;
        member.getChargeCoinList().add(this);
    }
}
