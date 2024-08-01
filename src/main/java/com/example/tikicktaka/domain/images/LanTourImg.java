package com.example.tikicktaka.domain.images;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.lanTour.LanTour;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LanTourImg extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lan_tour_img_id")
    private Long id;

    private String imgUrl;

    private Boolean thumbnail;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lan_tour_id", nullable = false)
    private LanTour lanTour;

    public void setLanTour(LanTour lanTour){
        if(this.lanTour != null)
            lanTour.getLanTourImgList().remove(this);
        this.lanTour = lanTour;
        lanTour.getLanTourImgList().add(this);
    }
}
