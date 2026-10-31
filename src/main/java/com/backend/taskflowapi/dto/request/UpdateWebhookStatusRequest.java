package com.backend.taskflowapi.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateWebhookStatusRequest(
        @NotNull(message = "Active flag is required!")
        Boolean active
) {}
