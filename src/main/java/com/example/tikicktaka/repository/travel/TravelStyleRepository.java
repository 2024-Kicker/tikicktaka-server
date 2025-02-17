package com.example.tikicktaka.repository.travel;

import com.example.tikicktaka.domain.travel.TravelStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelStyleRepository extends JpaRepository<TravelStyle, Long> {
    Optional<TravelStyle> findByName(String name); // 여행 스타일 이름으로 검색
}

