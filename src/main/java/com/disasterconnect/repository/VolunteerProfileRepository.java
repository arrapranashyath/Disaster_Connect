package com.disasterconnect.repository;

import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, Long> {

    Optional<VolunteerProfile> findByUser(User user);

    Optional<VolunteerProfile> findByUser_UserId(Long userId);

    java.util.List<VolunteerProfile> findByVerificationStatus(com.disasterconnect.entity.enums.VerificationStatus status);

    org.springframework.data.domain.Page<VolunteerProfile> findByVerificationStatus(com.disasterconnect.entity.enums.VerificationStatus status, org.springframework.data.domain.Pageable pageable);
}
