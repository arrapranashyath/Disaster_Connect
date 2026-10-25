package com.disasterconnect.repository;

import com.disasterconnect.entity.IncidentReport;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    /** All incidents submitted by a specific user (citizen's own reports), newest first. */
    Page<IncidentReport> findAllByReportedByOrderByCreatedAtDesc(User reportedBy, Pageable pageable);

    /** All incidents with a given status, for authority/admin views. */
    Page<IncidentReport> findAllByStatus(IncidentStatus status, Pageable pageable);

    /** All incidents, newest first (for authority/admin/ngo full listing). */
    Page<IncidentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
