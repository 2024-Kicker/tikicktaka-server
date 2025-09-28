package com.example.tikicktaka.repository.myTravelInning;

import com.example.tikicktaka.domain.MyTravelInning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyTravelInningRepository extends JpaRepository<MyTravelInning, Long> {
    List<MyTravelInning> findByMember_IdOrderByDateDesc(Long memberId);

}
