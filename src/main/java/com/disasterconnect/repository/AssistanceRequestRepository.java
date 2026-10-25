package com.disasterconnect.repository;

import com.disasterconnect.entity.AssistanceRequest;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.AssistanceType;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {

    /** Find requests created by a specific citizen, newest first */
    Page<AssistanceRequest> findAllByCitizenOrderByCreatedAtDesc(User citizen, Pageable pageable);

    /** Find requests assigned to a specific volunteer or responder, newest first */
    Page<AssistanceRequest> findAllByAssignedToOrderByCreatedAtDesc(User responder, Pageable pageable);

    /** Find requests assigned by a specific NGO coordinator */
    Page<AssistanceRequest> findAllByAssignedByOrderByCreatedAtDesc(User ngo, Pageable pageable);

    /** Find all requests by status */
    Page<AssistanceRequest> findAllByStatus(RequestStatus status, Pageable pageable);

    /** Count requests by status (for dashboards) */
    long countByStatus(RequestStatus status);

    /** Count requests assigned to a specific responder with given status */
    long countByAssignedToAndStatus(User responder, RequestStatus status);

    /** Count requests assigned by a specific NGO with given status */
    long countByAssignedByAndStatus(User ngo, RequestStatus status);

    /**
     * Flexible search/filter for open or filtered assistance requests with optional filters:
     * status, assistanceType, priority, location keyword.
     */
    @Query("SELECT r FROM AssistanceRequest r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:assistanceType IS NULL OR r.assistanceType = :assistanceType) AND " +
            "(:priority IS NULL OR r.priority = :priority) AND " +
            "(:location IS NULL OR LOWER(r.locationName) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<AssistanceRequest> searchRequests(@Param("status") RequestStatus status,
                                          @Param("assistanceType") AssistanceType assistanceType,
                                          @Param("priority") RequestPriority priority,
                                          @Param("location") String location,
                                          Pageable pageable);
}
