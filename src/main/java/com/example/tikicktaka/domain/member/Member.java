package com.example.tikicktaka.domain.member;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.enums.Gender;
import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.enums.SocialType;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
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
    @ColumnDefault("'member'")
    private MemberRole memberRole;

    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;

    @Column(columnDefinition = "VARCHAR(20)")
    private String name;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    private Long point;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<RegisterSeller> registerSellerList = new ArrayList<>();

    @OneToOne(mappedBy = "member")
    private MemberTeam memberTeam;

    public void setProfileImg(ProfileImg profileImg) {
        this.profileImg = profileImg;
    }

    public void setMemberStatus(MemberStatus memberStatus) { this.memberStatus = memberStatus; }

    public void setSocialType(SocialType socialType) { this.socialType = socialType; }


    public Member(String email, String nickname, String name, Date birthday, Gender gender, String phone, List<MemberTerm> memberTermList) {

    }
    public Member(String email, String nickname, List<MemberTerm> memberTermList) {

    }
    // Method to update additional information after social login
    public void updateAdditionalInfo(Date birthday, String phone) {
        this.birthday = birthday;
        this.phone = phone;
        this.memberStatus = MemberStatus.ACTIVE;
    }

//    // Method to update additional information after social login
//    public void updateAdditionalInfo(String name, Date birthday, Gender gender, String phone, List<MemberTerm> memberTermList) {
//        this.birthday = birthday;
//        this.gender = gender;
//        this.phone = phone;
//        this.memberTermList = memberTermList;
//        this.memberStatus = MemberStatus.ACTIVE;
//    }
}
