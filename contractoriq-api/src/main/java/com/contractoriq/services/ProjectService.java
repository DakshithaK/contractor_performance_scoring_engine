package com.contractoriq.services;

import com.contractoriq.dto.ProjectIngestionDTO;
import com.contractoriq.kafka.ScoreRecalculationProducer;
import com.contractoriq.models.Project;
import com.contractoriq.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ScoreRecalculationProducer producer;

    public ProjectService(ProjectRepository projectRepository,
                          ScoreRecalculationProducer producer) {
        this.projectRepository = projectRepository;
        this.producer = producer;
    }

    public Project ingest(ProjectIngestionDTO dto) {
        Project p = new Project();
        p.setId(UUID.randomUUID());
        p.setContractorId(dto.getContractorId());
        p.setProjectName(dto.getProjectName());
        p.setCity(dto.getCity());
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setPlannedEndDate(dto.getPlannedEndDate());
        p.setBudgetPlanned(dto.getBudgetPlanned());
        p.setBudgetActual(dto.getBudgetActual());
        p.setCustomerRating(dto.getCustomerRating());
        p.setCompletionStatus(dto.getCompletionStatus());
        Project saved = projectRepository.save(p);
        producer.publish(saved.getContractorId(), "PROJECT_ADDED");
        return saved;
    }

    public List<Project> findByContractor(UUID contractorId) {
        return projectRepository.findByContractorId(contractorId);
    }
}
