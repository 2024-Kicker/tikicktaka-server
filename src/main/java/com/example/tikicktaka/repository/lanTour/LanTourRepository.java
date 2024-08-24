package com.example.tikicktaka.repository.lanTour;

import com.example.tikicktaka.domain.lanTour.LanTour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanTourRepository extends JpaRepository<LanTour, Long> {
    Page<LanTour> findAllByLocation(String Location, PageRequest pageRequest);
}
