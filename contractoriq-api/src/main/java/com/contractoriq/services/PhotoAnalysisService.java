package com.contractoriq.services;

import com.contractoriq.dto.VisionAnalysisDTO;
import com.contractoriq.kafka.ScoreRecalculationProducer;
import com.contractoriq.models.SitePhotoAnalysis;
import com.contractoriq.repositories.SitePhotoAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
public class PhotoAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PhotoAnalysisService.class);

    private final VisionServiceClient visionServiceClient;
    private final SitePhotoAnalysisRepository repository;
    private final ScoreRecalculationProducer producer;

    @Value("${contractoriq.photo-storage-dir:./photos}")
    private String storageDir;

    public PhotoAnalysisService(VisionServiceClient visionServiceClient,
                                SitePhotoAnalysisRepository repository,
                                ScoreRecalculationProducer producer) {
        this.visionServiceClient = visionServiceClient;
        this.repository = repository;
        this.producer = producer;
    }

    public SitePhotoAnalysis analyzeAndStore(UUID contractorId,
                                             UUID projectId,
                                             MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String savedPath = persistToDisk(contractorId, file, bytes);

        VisionAnalysisDTO analysis = visionServiceClient.analyzePhoto(
                bytes, file.getOriginalFilename(), file.getContentType());

        SitePhotoAnalysis entity = new SitePhotoAnalysis();
        entity.setId(UUID.randomUUID());
        entity.setContractorId(contractorId);
        entity.setProjectId(projectId);
        entity.setPhotoPath(savedPath);
        entity.setQualityScore(analysis.getQualityScore());
        entity.setClaudeRawResponse(analysis.getRaw());
        entity.setIssuesFound(analysis.getIssues());
        entity.setAnalyzedAt(Instant.now());

        SitePhotoAnalysis saved = repository.save(entity);
        producer.publish(contractorId, "PHOTO_ANALYZED");
        return saved;
    }

    private String persistToDisk(UUID contractorId, MultipartFile file, byte[] bytes)
            throws IOException {
        Path dir = Paths.get(storageDir, contractorId.toString());
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "-" + (file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "photo.jpg");
        Path target = dir.resolve(name);
        Files.write(target, bytes);
        log.info("stored photo for contractor {} at {}", contractorId, target);
        return target.toString();
    }
}
