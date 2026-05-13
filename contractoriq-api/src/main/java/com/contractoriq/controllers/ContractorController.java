package com.contractoriq.controllers;

import com.contractoriq.dto.ContractorScoreDTO;
import com.contractoriq.models.Contractor;
import com.contractoriq.services.ContractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contractors")
public class ContractorController {

    private final ContractorService contractorService;

    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    @GetMapping
    public List<ContractorScoreDTO> listAll() {
        return contractorService.listAllWithScores();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractorScoreDTO> getById(@PathVariable UUID id) {
        ContractorScoreDTO dto = contractorService.getScoreDTO(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public Contractor create(@RequestBody Contractor contractor) {
        return contractorService.create(contractor);
    }
}
