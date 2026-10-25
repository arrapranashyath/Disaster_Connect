package com.disasterconnect.repository;

import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NGOProfileRepository extends JpaRepository<NGOProfile, Long> {

    Optional<NGOProfile> findByUser(User user);

    Optional<NGOProfile> findByUser_UserId(Long userId);

    java.util.List<NGOProfile> findByVerificationStatus(com.disasterconnect.entity.enums.VerificationStatus status);

    org.springframework.data.domain.Page<NGOProfile> findByVerificationStatus(com.disasterconnect.entity.enums.VerificationStatus status, org.springframework.data.domain.Pageable pageable);
}
