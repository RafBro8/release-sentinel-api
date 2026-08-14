package com.releasesentinel.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasesentinel.api.dto.CreateDefectRequest;
import com.releasesentinel.api.dto.DefectResponse;
import com.releasesentinel.api.dto.UpdateDefectStatusRequest;
import com.releasesentinel.domain.DefectPriority;
import com.releasesentinel.domain.DefectSeverity;
import com.releasesentinel.domain.DefectStatus;
import com.releasesentinel.service.DefectService;
import com.releasesentinel.service.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DefectController.class)
class DefectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DefectService defectService;

    @Test
    void createDefectReturnsCreatedDefect() throws Exception {
        UUID releaseId = UUID.randomUUID();
        DefectResponse response = new DefectResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                releaseId,
                null,
                "Checkout returns 500",
                "Payment confirmation fails after authorization",
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                DefectStatus.OPEN,
                true,
                Instant.parse("2026-08-13T12:00:00Z"),
                Instant.parse("2026-08-13T12:00:00Z"));

        when(defectService.createDefect(eq(releaseId), any(CreateDefectRequest.class))).thenReturn(response);

        CreateDefectRequest request = new CreateDefectRequest(
                "Checkout returns 500",
                "Payment confirmation fails after authorization",
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null,
                null);

        mockMvc.perform(post("/api/releases/{releaseId}/defects", releaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Checkout returns 500"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.blockingRelease").value(true));
    }

    @Test
    void createDefectRejectsMissingTitle() throws Exception {
        UUID releaseId = UUID.randomUUID();
        CreateDefectRequest request = new CreateDefectRequest(
                "",
                "Missing title should fail validation",
                DefectSeverity.HIGH,
                DefectPriority.HIGH,
                null,
                null);

        mockMvc.perform(post("/api/releases/{releaseId}/defects", releaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void getDefectReturnsNotFoundContract() throws Exception {
        UUID defectId = UUID.randomUUID();
        when(defectService.getDefect(defectId))
                .thenThrow(new ResourceNotFoundException("Defect not found: " + defectId));

        mockMvc.perform(get("/api/defects/{defectId}", defectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Defect not found: " + defectId));
    }

    @Test
    void updateStatusRejectsMissingStatus() throws Exception {
        UUID defectId = UUID.randomUUID();
        UpdateDefectStatusRequest request = new UpdateDefectStatusRequest(null);

        mockMvc.perform(patch("/api/defects/{defectId}/status", defectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }
}
