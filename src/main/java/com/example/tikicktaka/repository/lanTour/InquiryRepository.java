package com.example.tikicktaka.repository.lanTour;

import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findAllByLanTour(LanTour lanTour, PageRequest pageRequest);

    Page<Inquiry> findAllByMember(Member member, PageRequest pageRequest);
}
