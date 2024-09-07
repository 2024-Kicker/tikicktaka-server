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
public class WideAreaData extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wide_area_data_id")
    private Long id;

    private Long areaCode;

    private String areaNm;

    private Long daywkDivCd;

    private String daywkDivNm;

    private Long touDivCd;

    private String touDivNm;

    private Double touNum;

    private String baseYmd;
}
