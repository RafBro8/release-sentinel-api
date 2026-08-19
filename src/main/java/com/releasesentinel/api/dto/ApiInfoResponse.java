package com.releasesentinel.api.dto;

public record ApiInfoResponse(
        String service,
        String statusUrl,
        String healthUrl,
        String docsUrl) {
}
