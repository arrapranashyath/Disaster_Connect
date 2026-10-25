package com.disasterconnect.repository;

import com.disasterconnect.entity.ReliefResource;
import com.disasterconnect.entity.ResourceUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceUpdateLogRepository extends JpaRepository<ResourceUpdateLog, Long> {

    List<ResourceUpdateLog> findAllByResourceOrderByCreatedAtDesc(ReliefResource resource);

    List<ResourceUpdateLog> findAllByResourceOrderByCreatedAtAsc(ReliefResource resource);
}
