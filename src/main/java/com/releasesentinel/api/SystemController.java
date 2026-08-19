package com.releasesentinel.api;

import com.releasesentinel.api.dto.ApiInfoResponse;
import com.releasesentinel.api.dto.ApiStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SystemController {

    @GetMapping("/")
    public ApiInfoResponse getApiInfo() {
        return new ApiInfoResponse(
                "Release Sentinel API",
                "/api/status",
                "/actuator/health",
                "/swagger-ui/index.html");
    }

    @GetMapping("/api/status")
    public ApiStatusResponse getStatus() {
        return new ApiStatusResponse("Release Sentinel API", "UP");
    }
}
