package com.disasterconnect.repository;

import com.disasterconnect.entity.DisasterAlert;
import com.disasterconnect.entity.enums.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisasterAlertRepository extends JpaRepository<DisasterAlert, Long> {

    /** Return all alerts with the given status, newest first (for public listing). */
    List<DisasterAlert> findAllByStatusOrderByIssuedAtDesc(AlertStatus status);

    /** Paged listing of all alerts, newest first. */
    Page<DisasterAlert> findAllByOrderByIssuedAtDesc(Pageable pageable);

    /** Paged listing filtered by status. */
    Page<DisasterAlert> findAllByStatus(AlertStatus status, Pageable pageable);

    /** Paged listing filtered by affected area (case-insensitive substring) and status. */
    Page<DisasterAlert> findAllByAffectedAreaContainingIgnoreCaseAndStatus(
            String affectedArea, AlertStatus status, Pageable pageable);

    /** Paged listing filtered by affected area only (no status filter). */
    Page<DisasterAlert> findAllByAffectedAreaContainingIgnoreCase(String affectedArea, Pageable pageable);
}
