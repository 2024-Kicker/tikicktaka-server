// src/main/java/com/example/tikicktaka/domain/mapping/scrap/TravelRegionScrap.java
package com.example.tikicktaka.domain.mapping.scrap;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.travel.TravelRegion;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "travel_region_scrap",
        uniqueConstraints = @UniqueConstraint(name = "uk_region_scrap_member_region",
                columnNames = {"member_id", "travel_region_id"}),
        indexes = {
                @Index(name = "idx_region_scrap_member", columnList = "member_id"),
                @Index(name = "idx_region_scrap_region", columnList = "travel_region_id")
        })
public class TravelRegionScrap extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_region_scrap_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "travel_region_id", nullable = false)
    private TravelRegion travelRegion;
}
