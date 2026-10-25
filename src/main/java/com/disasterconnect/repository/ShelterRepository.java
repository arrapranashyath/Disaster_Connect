package com.disasterconnect.repository;

import com.disasterconnect.entity.Shelter;
import com.disasterconnect.entity.enums.ServiceType;
import com.disasterconnect.entity.enums.ShelterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {

    @Query("SELECT s FROM Shelter s WHERE " +
           "(:type IS NULL OR s.serviceType = :type) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:city IS NULL OR LOWER(s.city) = LOWER(:city) OR LOWER(s.address) LIKE LOWER(CONCAT('%', :city, '%')))")
    Page<Shelter> searchShelters(@Param("type") ServiceType type,
                                 @Param("status") ShelterStatus status,
                                 @Param("city") String city,
                                 Pageable pageable);

    @Query("SELECT s, " +
           "(6371.0 * acos(cos(radians(:latitude)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(s.latitude)))) AS distance " +
           "FROM Shelter s WHERE " +
           "(:type IS NULL OR s.serviceType = :type) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY (6371.0 * acos(cos(radians(:latitude)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(s.latitude)))) ASC")
    List<Object[]> findNearbyWithDistance(@Param("latitude") Double latitude,
                                         @Param("longitude") Double longitude,
                                         @Param("type") ServiceType type,
                                         @Param("status") ShelterStatus status,
                                         Pageable pageable);

    List<Shelter> findAllByStatus(ShelterStatus status);
}
