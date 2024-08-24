package com.example.tikicktaka.domain.lanTour;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.enums.InquiryStatus;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Inquiry extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contents;

    private Boolean secret;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15)")
    @ColumnDefault("'BEFORE'")
    private InquiryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lan_tour_id", nullable = false)
    private LanTour lanTour;

    public void setInquiryStatus(InquiryStatus inquiryStatus) { this.status = inquiryStatus; }
}
