package com.backend.taskflowapi.service;

import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.Task;
import com.backend.taskflowapi.entity.WebhookEvenType;
import com.backend.taskflowapi.entity.WebhookSubscription;
import com.backend.taskflowapi.repository.WebhookSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WebhookDispatchService {

    private final static Logger log = LoggerFactory.getLogger(WebhookDispatchService.class);

    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final RestTemplate restTemplate;

    public WebhookDispatchService(
            WebhookSubscriptionRepository webhookSubscriptionRepository,
            RestTemplate restTemplate
    ) {
        this.webhookSubscriptionRepository = webhookSubscriptionRepository;
        this.restTemplate = restTemplate;
    }

    @Async
    public void dispatchTaskCompleted(Task task){
        Organization organization = task.getProject().getOrganization();

        List<WebhookSubscription> subscriptions = webhookSubscriptionRepository
                .findByOrganizationAndEvenTypeAndActiveTrue(organization, WebhookEvenType.TASK_COMPLETED);

        for (WebhookSubscription subscription : subscriptions){
            sendWebhook(subscription, task);
        }
    }

    private void sendWebhook(WebhookSubscription subscription, Task task){
        Map<String, Object> payload = Map.of(
                "event", "TASK_COMPLETED",
                "taskId", task.getId(),
                "taskTitle", task.getTitle(),
                "projectId", task.getProject().getId()
        );

        try {
            restTemplate.postForEntity(subscription.getTargetUrl(), payload, String.class);
            log.info("Webhook delivered successfully to {}", subscription.getTargetUrl());
        } catch (Exception e){
            log.info("Webhook delivery failed for {}: {}", subscription.getTargetUrl(), e.getMessage());
        }
    }
}
