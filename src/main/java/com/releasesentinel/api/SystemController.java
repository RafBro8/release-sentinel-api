package com.releasesentinel.api;

import com.releasesentinel.api.dto.ApiStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemController {

    @GetMapping("/status")
    public ApiStatusResponse getStatus() {
        return new ApiStatusResponse("Release Sentinel API", "UP");
    }
}
