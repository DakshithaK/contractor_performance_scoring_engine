package com.contractoriq.controllers;

import com.contractoriq.models.SitePhotoAnalysis;
import com.contractoriq.services.PhotoAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoAnalysisService photoAnalysisService;

    public PhotoController(PhotoAnalysisService photoAnalysisService) {
        this.photoAnalysisService = photoAnalysisService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SitePhotoAnalysis> analyze(
            @RequestParam("contractorId") UUID contractorId,
            @RequestParam(value = "projectId", required = false) UUID projectId,
            @RequestPart("file") MultipartFile file) throws IOException {
        SitePhotoAnalysis analysis = photoAnalysisService.analyzeAndStore(contractorId, projectId, file);
        return ResponseEntity.ok(analysis);
    }
}
