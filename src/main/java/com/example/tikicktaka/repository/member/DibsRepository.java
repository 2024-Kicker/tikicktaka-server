package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.mapping.member.Dibs;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DibsRepository extends JpaRepository<Dibs, Long> {
    Optional<Dibs> findByLanTourAndMember(LanTour lanTour, Member member);

    Page<Dibs> findAllByMember(Member member, PageRequest pageRequest);
}
