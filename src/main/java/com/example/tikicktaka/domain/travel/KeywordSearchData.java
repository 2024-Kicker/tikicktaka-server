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
public class KeywordSearchData extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_search_data_id")
    private Long id;

    private Long tid;

    private Long tlid;

    private String addr1;

    private String add2;

    private String title;

    private Double mapX;

    private Double mapY;

    private Long langCheck;

    private String langCode;

    private String imageUrl;

    private String createdTime;

    private String modifiedTime;
}
