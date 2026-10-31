package com.backend.taskflowapi.service;

import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.Task;
import com.backend.taskflowapi.entity.WebhookDelivery;
import com.backend.taskflowapi.entity.WebhookSubscription;
import com.backend.taskflowapi.enums.WebhookEvenType;
import com.backend.taskflowapi.repository.WebhookDeliveryRepository;
import com.backend.taskflowapi.repository.WebhookSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WebhookDispatchService {

    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final ObjectMapper objectMapper;


    public WebhookDispatchService(
            WebhookSubscriptionRepository webhookSubscriptionRepository,
            WebhookDeliveryRepository webhookDeliveryRepository,
            ObjectMapper objectMapper
    ) {
        this.webhookSubscriptionRepository = webhookSubscriptionRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void dispatchTaskCompleted(Task task){
        Organization organization = task.getProject().getOrganization();

        List<WebhookSubscription> subscriptions = webhookSubscriptionRepository
                .findByOrganizationAndEvenTypeAndActiveTrue(organization, WebhookEvenType.TASK_COMPLETED);

        if (subscriptions.isEmpty()){
            return;
        }


        String payload = buildPayload(task);

        for (WebhookSubscription subscription : subscriptions){
            webhookDeliveryRepository.save(
                    new WebhookDelivery(subscription, WebhookEvenType.TASK_COMPLETED, payload)
            );
        }
    }

    private String buildPayload(Task task){
        Map<String, Object> data = Map.of(
                "event", "TASK_COMPLETED",
                "taskId", task.getId(),
                "taskTitle", task.getTitle(),
                "projectId", task.getProject().getId()
        );

        return objectMapper.writeValueAsString(data);
    }
}
