package com.backend.taskflowapi.mapper;

import com.backend.taskflowapi.dto.response.WebhookResponse;
import com.backend.taskflowapi.entity.WebhookSubscription;
import org.springframework.stereotype.Component;

@Component
public class WebhookMapper {

    public WebhookResponse toResponse(WebhookSubscription subscription){
        return new WebhookResponse(
                subscription.getId(),
                subscription.getTargetUrl(),
                subscription.getEvenType(),
                subscription.isActive(),
                subscription.getCreatedAt()
        );
    }
}
