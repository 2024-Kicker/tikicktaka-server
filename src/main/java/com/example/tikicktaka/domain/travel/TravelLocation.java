package com.example.tikicktaka.domain.travel;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TravelLocation extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_location_id")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String addr1;

    @Column(columnDefinition = "TEXT")
    private String addr2;

    private Long areaCode;

    private Long bookTour;

    private String cat1;

    private String cat2;

    private String cat3;

    private Long contentId;

    private Long contentTypeId;

    private String createdTime;

    private Double dist;

    @Column(columnDefinition = "TEXT")
    private String firstImage;

    @Column(columnDefinition = "TEXT")
    private String firstImage2;

    private String cpyrhtDivCd;

    private Double mapX;

    private Double mayY;

    private Long mLevel;

    private String modifiedTime;

    private Long siGunGuCode;

    private String tel;

    private String title;
}
