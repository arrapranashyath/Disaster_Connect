package com.disasterconnect.repository;

import com.disasterconnect.entity.MissingPerson;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.MissingPersonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissingPersonRepository extends JpaRepository<MissingPerson, Long> {

    List<MissingPerson> findByStatus(MissingPersonStatus status);

    List<MissingPerson> findByReportedBy(User reportedBy);

    @Query("SELECT m FROM MissingPerson m WHERE " +
           "(:status IS NULL OR m.status = :status) AND (" +
           ":query IS NULL OR :query = '' OR " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.lastSeenLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<MissingPerson> searchMissingPersons(@Param("query") String query,
                                            @Param("status") MissingPersonStatus status,
                                            Pageable pageable);

    @Query("SELECT m FROM MissingPerson m WHERE " +
           "(:status IS NULL OR m.status = :status) AND (" +
           ":query IS NULL OR :query = '' OR " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.lastSeenLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<MissingPerson> searchMissingPersonsList(@Param("query") String query,
                                                @Param("status") MissingPersonStatus status);
}
