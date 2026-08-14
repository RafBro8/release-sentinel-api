package com.releasesentinel.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.releasesentinel.api.dto.ReleaseQualitySummaryResponse;
import com.releasesentinel.domain.QualityStatus;
import com.releasesentinel.service.QualityGateService;
import com.releasesentinel.service.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QualityGateController.class)
class QualityGateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QualityGateService qualityGateService;

    @Test
    void getReleaseQualitySummaryReturnsSummary() throws Exception {
        UUID releaseId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ReleaseQualitySummaryResponse response = new ReleaseQualitySummaryResponse(
                releaseId,
                projectId,
                "1.0.0",
                QualityStatus.READY,
                10,
                10,
                0,
                0,
                0,
                100.0,
                0,
                0,
                0,
                0,
                "Release is ready to ship based on current test and defect signals.",
                List.of());

        when(qualityGateService.getReleaseQualitySummary(releaseId)).thenReturn(response);

        mockMvc.perform(get("/api/releases/{releaseId}/quality-summary", releaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseVersion").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.passRate").value(100.0));
    }

    @Test
    void getReleaseQualitySummaryReturnsNotFoundContract() throws Exception {
        UUID releaseId = UUID.randomUUID();
        when(qualityGateService.getReleaseQualitySummary(releaseId))
                .thenThrow(new ResourceNotFoundException("Release not found: " + releaseId));

        mockMvc.perform(get("/api/releases/{releaseId}/quality-summary", releaseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Release not found: " + releaseId));
    }
}
