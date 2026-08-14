package com.releasesentinel.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasesentinel.api.dto.CreateTestCaseRequest;
import com.releasesentinel.api.dto.TestCaseResponse;
import com.releasesentinel.domain.TestPriority;
import com.releasesentinel.domain.TestType;
import com.releasesentinel.service.TestExecutionService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestCaseController.class)
class TestCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestExecutionService testExecutionService;

    @Test
    void createTestCaseReturnsCreatedTestCase() throws Exception {
        UUID projectId = UUID.randomUUID();
        TestCaseResponse response = new TestCaseResponse(
                UUID.randomUUID(),
                projectId,
                "Login API rejects invalid password",
                "Verify invalid credentials return 401",
                TestPriority.HIGH,
                TestType.API,
                true,
                Instant.parse("2026-08-13T12:00:00Z"));

        when(testExecutionService.createTestCase(eq(projectId), any(CreateTestCaseRequest.class)))
                .thenReturn(response);

        CreateTestCaseRequest request = new CreateTestCaseRequest(
                "Login API rejects invalid password",
                "Verify invalid credentials return 401",
                TestPriority.HIGH,
                TestType.API);

        mockMvc.perform(post("/api/projects/{projectId}/test-cases", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Login API rejects invalid password"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.type").value("API"));
    }

    @Test
    void createTestCaseRejectsMissingTitle() throws Exception {
        UUID projectId = UUID.randomUUID();
        CreateTestCaseRequest request = new CreateTestCaseRequest(
                "",
                "Missing title should be rejected",
                TestPriority.HIGH,
                TestType.API);

        mockMvc.perform(post("/api/projects/{projectId}/test-cases", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }
}
