package com.disasterconnect.repository;

import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.ReliefResource;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.ResourceStatus;
import com.disasterconnect.entity.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReliefResourceRepository extends JpaRepository<ReliefResource, Long> {

    @Query("SELECT r FROM ReliefResource r WHERE " +
           "(:type IS NULL OR r.resourceType = :type) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:priority IS NULL OR r.priority = :priority) AND " +
           "(:location IS NULL OR LOWER(r.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<ReliefResource> searchResources(@Param("type") ResourceType type,
                                         @Param("status") ResourceStatus status,
                                         @Param("priority") RequestPriority priority,
                                         @Param("location") String location,
                                         Pageable pageable);

    Page<ReliefResource> findAllByProvidedByOrderByCreatedAtDesc(NGOProfile providedBy, Pageable pageable);

    List<ReliefResource> findAllByLocationContainingIgnoreCase(String location);

    @Query("SELECT r FROM ReliefResource r WHERE LOWER(r.location) LIKE LOWER(CONCAT('%', :area, '%')) AND r.status IN ('LOW', 'CRITICAL')")
    List<ReliefResource> findShortagesByArea(@Param("area") String area);
}
