package com.disasterconnect.repository;

import com.disasterconnect.entity.AssistanceRequest;
import com.disasterconnect.entity.RequestStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestStatusLogRepository extends JpaRepository<RequestStatusLog, Long> {

    List<RequestStatusLog> findAllByRequestOrderByCreatedAtAsc(AssistanceRequest request);
}
