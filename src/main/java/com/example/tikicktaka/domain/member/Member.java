package com.example.tikicktaka.domain.member;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.enums.Gender;
import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.enums.SocialType;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.mapping.member.MemberTerm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private MemberRole memberRole;

    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;

    @Column(columnDefinition = "VARCHAR(20)")
    private String name;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String loginId;

    @Column(columnDefinition = "VARCHAR(50)")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(columnDefinition = "VARCHAR(13)")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(10)")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @OneToOne(mappedBy = "member")
    private ProfileImg profileImg;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15)")
    @ColumnDefault("'ACTIVE'")
    private MemberStatus memberStatus;

    @Column(columnDefinition = "TEXT")
    private String kakaoAuth;

    @Column(columnDefinition = "TEXT")
    private String googleAuth;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberTerm> memberTermList = new ArrayList<>();

    public void setProfileImg(ProfileImg profileImg) {
        this.profileImg = profileImg;
    }
}
