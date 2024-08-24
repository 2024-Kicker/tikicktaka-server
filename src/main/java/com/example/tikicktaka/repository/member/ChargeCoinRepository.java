package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.mapping.member.ChargeCoin;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeCoinRepository extends JpaRepository<ChargeCoin, Long> {

    Page<ChargeCoin> findAllByMember(Member member, PageRequest pageRequest);
}
