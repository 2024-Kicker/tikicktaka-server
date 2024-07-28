package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.mapping.member.ChargeCoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeCoinRepository extends JpaRepository<ChargeCoin, Long> {
}
