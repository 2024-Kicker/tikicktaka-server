package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.images.ProfileImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileImgRepository extends JpaRepository<ProfileImg, Long> {

    Optional<ProfileImg> findByMember_Id(Long id);
}
