package com.example.tikicktaka.domain.member;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.mapping.member.MemberTerm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Term extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long id;

    @Column(columnDefinition = "VARCHAR(30)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(columnDefinition = "BOOLEAN")
    @ColumnDefault("false")
    private Boolean essential;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<MemberTerm> memberTermList = new ArrayList<>();
}
