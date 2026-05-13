package com.contractoriq.services;

import com.contractoriq.dto.VisionAnalysisDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class VisionServiceClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private VisionServiceClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplateBuilder().build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        client = new VisionServiceClient(restTemplate, "http://vision-service:8001");
    }

    @Test
    void testSuccessfulVisionServiceCall() {
        String body = "{"
                + "\"quality_score\":88,"
                + "\"cleanliness\":80,"
                + "\"safety_compliance\":90,"
                + "\"work_quality\":85,"
                + "\"issues\":[],"
                + "\"positive_observations\":[\"clean site\"],"
                + "\"overall_summary\":\"well managed\""
                + "}";

        mockServer.expect(requestTo("http://vision-service:8001/analyze-photo"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        VisionAnalysisDTO dto = client.analyzePhoto("fake".getBytes(), "p.jpg", "image/jpeg");

        assertThat(dto.getQualityScore()).isEqualTo(88);
        assertThat(dto.getCleanliness()).isEqualTo(80);
        assertThat(dto.getSafetyCompliance()).isEqualTo(90);
        assertThat(dto.getWorkQuality()).isEqualTo(85);
        assertThat(dto.getOverallSummary()).isEqualTo("well managed");
        mockServer.verify();
    }

    @Test
    void testVisionServiceTimeoutHandledGracefully() {
        mockServer.expect(requestTo("http://vision-service:8001/analyze-photo"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(req -> { throw new IOException("connect timeout"); });

        assertThatThrownBy(() ->
                client.analyzePhoto("fake".getBytes(), "p.jpg", "image/jpeg"))
                .isInstanceOf(VisionServiceClient.VisionServiceException.class)
                .hasMessageContaining("unreachable")
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @Test
    void testGenerateRecommendationReturnsReasoning() {
        mockServer.expect(requestTo("http://vision-service:8001/generate-recommendation"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"reasoning\":\"Recommended for high-value projects\"}",
                        MediaType.APPLICATION_JSON));

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "ACME");
        String reasoning = client.generateRecommendation(payload);

        assertThat(reasoning).isEqualTo("Recommended for high-value projects");
    }

    @Test
    void testVisionServiceReturns500() {
        mockServer.expect(requestTo("http://vision-service:8001/analyze-photo"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() ->
                client.analyzePhoto("fake".getBytes(), "p.jpg", "image/jpeg"))
                .isInstanceOf(VisionServiceClient.VisionServiceException.class);
    }
}
