package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.member.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByPhone(String phone);

    Optional<Auth> findByEmail(String email);
}
