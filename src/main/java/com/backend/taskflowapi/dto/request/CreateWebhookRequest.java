package com.backend.taskflowapi.dto.request;

import com.backend.taskflowapi.entity.WebhookEvenType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateWebhookRequest(

        @NotBlank(message = "Target Url is required!")
        @Pattern(regexp = "^https?://.*", message = "Target URL must start with http:// or https://")
        String targetUrl,

        @NotNull(message = "EventType is required!")
        WebhookEvenType evenType
) {
}
