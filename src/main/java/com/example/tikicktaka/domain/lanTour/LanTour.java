package com.example.tikicktaka.domain.lanTour;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.enums.LanTourCategory;
import com.example.tikicktaka.domain.images.LanTourImg;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LanTour extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lan_tour__id")
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(columnDefinition = "TEXT")
    private String location;

    private Long price;

    @Enumerated(EnumType.STRING)
    private LanTourCategory lanTourCategory;

    private Long salesCount;

    private Long dibsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "lanTour", cascade = CascadeType.ALL)
    private List<LanTourImg> lanTourImgList = new ArrayList<>();

    @OneToMany(mappedBy = "lanTour", cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "lanTour", cascade = CascadeType.ALL)
    private List<Inquiry> inquiryList = new ArrayList<>();

    @OneToMany(mappedBy = "lanTour", cascade = CascadeType.ALL)
    private List<LanTourPurchase> lanTourPurchaseList = new ArrayList<>();

    public LanTour updateSales() {
        this.salesCount += 1;
        return this;
    }

    public LanTour increaseDibs(){
        this.dibsCount += 1;
        return this;
    }

    public LanTour decreaseDibs(){
        this.dibsCount -= 1;
        return this;
    }
}
