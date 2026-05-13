package com.contractoriq.controllers;

import com.contractoriq.dto.ProjectIngestionDTO;
import com.contractoriq.models.Project;
import com.contractoriq.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Project ingest(@Valid @RequestBody ProjectIngestionDTO dto) {
        return projectService.ingest(dto);
    }

    @GetMapping("/contractor/{contractorId}")
    public List<Project> byContractor(@PathVariable UUID contractorId) {
        return projectService.findByContractor(contractorId);
    }
}
