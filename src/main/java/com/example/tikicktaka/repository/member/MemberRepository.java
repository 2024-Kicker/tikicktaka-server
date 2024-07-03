package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.enums.Gender;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByPhone(String phone);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.name = :name, m.loginId = :loginId, m.email = :email, m.phone = :phone, m.birthday = :birthday, m.gender = :gender, m.password = :password, m.nickname = :nickname WHERE m.id = :memberId")
    void reregister(Long memberId, String nickname, String name, String password, String loginId, String email, Date birthday
            , Gender gender, String phone);
}
