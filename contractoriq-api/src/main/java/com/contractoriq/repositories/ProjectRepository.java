package com.contractoriq.repositories;

import com.contractoriq.models.CompletionStatus;
import com.contractoriq.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByContractorId(UUID contractorId);

    List<Project> findByContractorIdAndCompletionStatus(UUID contractorId, CompletionStatus status);
}
