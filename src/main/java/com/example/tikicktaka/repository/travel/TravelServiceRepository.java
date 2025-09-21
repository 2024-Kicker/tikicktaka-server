// src/main/java/com/example/tikicktaka/repository/travel/TravelServiceRepository.java
package com.example.tikicktaka.repository.travel;

import com.example.tikicktaka.domain.travel.TravelService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TravelServiceRepository extends JpaRepository<TravelService, Long> {

    // 필요한 필드만 받는 Close Projection
    interface CodeOnly {
        String getCode();
    }

    // travelStyle.id IN (:styleIds)
    List<CodeOnly> findByTravelStyle_IdIn(List<Long> styleIds);
}
