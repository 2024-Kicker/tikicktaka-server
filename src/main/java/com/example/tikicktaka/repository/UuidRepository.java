package com.example.tikicktaka.repository;

import com.example.tikicktaka.domain.images.Uuid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UuidRepository extends JpaRepository<Uuid, Long> {
}
