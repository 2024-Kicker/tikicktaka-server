package com.example.tikicktaka.domain.mapping.member;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.member.Term;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberTerm extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_term_id")
    private Long id;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    @ColumnDefault("false")
    private Boolean memberAgree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    public void setMember(Member member) {
        if (this.member != null)
            member.getMemberTermList().remove(this);
        this.member = member;
        member.getMemberTermList().add(this);
    }
}
