package com.contractoriq.services;

import com.contractoriq.dto.VisionAnalysisDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VisionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(VisionServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public VisionServiceClient(RestTemplate visionServiceRestTemplate,
                               @Value("${vision.service.url}") String baseUrl) {
        this.restTemplate = visionServiceRestTemplate;
        this.baseUrl = baseUrl;
    }

    @SuppressWarnings("unchecked")
    public VisionAnalysisDTO analyzePhoto(byte[] imageBytes, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "photo.jpg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(
                contentType != null ? contentType : MediaType.IMAGE_JPEG_VALUE));
        body.add("file", new HttpEntity<>(fileResource, partHeaders));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> resp = restTemplate.postForObject(
                    baseUrl + "/analyze-photo", request, Map.class);
            return mapToDTO(resp);
        } catch (ResourceAccessException ex) {
            log.warn("vision-service unreachable: {}", ex.getMessage());
            throw new VisionServiceException("vision-service unreachable", ex);
        } catch (Exception ex) {
            log.warn("vision-service call failed: {}", ex.getMessage());
            throw new VisionServiceException("vision-service call failed", ex);
        }
    }

    public String generateRecommendation(Map<String, Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(
                    baseUrl + "/generate-recommendation", entity, Map.class);
            if (resp == null) return null;
            Object reasoning = resp.get("reasoning");
            return reasoning != null ? reasoning.toString() : null;
        } catch (ResourceAccessException ex) {
            log.warn("recommendation call timed out: {}", ex.getMessage());
            throw new VisionServiceException("recommendation call timed out", ex);
        } catch (Exception ex) {
            log.warn("recommendation call failed: {}", ex.getMessage());
            throw new VisionServiceException("recommendation call failed", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private VisionAnalysisDTO mapToDTO(Map<String, Object> resp) {
        VisionAnalysisDTO dto = new VisionAnalysisDTO();
        if (resp == null) return dto;
        dto.setQualityScore(asInt(resp.get("quality_score")));
        dto.setCleanliness(asInt(resp.get("cleanliness")));
        dto.setSafetyCompliance(asInt(resp.get("safety_compliance")));
        dto.setWorkQuality(asInt(resp.get("work_quality")));
        dto.setIssues((List<Map<String, Object>>) resp.getOrDefault("issues", List.of()));
        dto.setPositiveObservations((List<String>) resp.getOrDefault("positive_observations", List.of()));
        Object summary = resp.get("overall_summary");
        dto.setOverallSummary(summary != null ? summary.toString() : "");
        Map<String, Object> raw = new HashMap<>(resp);
        dto.setRaw(raw);
        return dto;
    }

    private static int asInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    public static class VisionServiceException extends RuntimeException {
        public VisionServiceException(String msg, Throwable cause) { super(msg, cause); }
    }
}
