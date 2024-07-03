package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.member.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}
