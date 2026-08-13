package com.releasesentinel.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasesentinel.api.dto.CreateProjectRequest;
import com.releasesentinel.api.dto.ProjectResponse;
import com.releasesentinel.service.ReleaseTrackingService;
import com.releasesentinel.service.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReleaseTrackingService releaseTrackingService;

    @Test
    void createProjectReturnsCreatedProject() throws Exception {
        ProjectResponse response = new ProjectResponse(
                UUID.randomUUID(),
                "SHOP",
                "Shopping Platform",
                "Checkout and account management",
                Instant.parse("2026-08-12T12:00:00Z"));

        when(releaseTrackingService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        CreateProjectRequest request = new CreateProjectRequest(
                "SHOP",
                "Shopping Platform",
                "Checkout and account management");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("SHOP"))
                .andExpect(jsonPath("$.name").value("Shopping Platform"));
    }

    @Test
    void createProjectRejectsInvalidProjectKey() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "shop",
                "Shopping Platform",
                null);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.key").exists());
    }

    @Test
    void getProjectReturnsNotFoundErrorContract() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(releaseTrackingService.getProject(projectId))
                .thenThrow(new ResourceNotFoundException("Project not found: " + projectId));

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found: " + projectId));
    }
}
