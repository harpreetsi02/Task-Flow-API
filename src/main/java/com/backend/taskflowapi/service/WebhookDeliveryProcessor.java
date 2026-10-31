package com.backend.taskflowapi.service;

import com.backend.taskflowapi.entity.WebhookDelivery;
import com.backend.taskflowapi.enums.WebhookDeliveryStatus;
import com.backend.taskflowapi.repository.WebhookDeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WebhookDeliveryProcessor {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryProcessor.class);

    private static final long[] BACKOFF_MINUTES = {1, 5, 30, 120};
    private static final int MAX_ATTEMPTS = BACKOFF_MINUTES.length + 1;

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final RestTemplate restTemplate;

    public WebhookDeliveryProcessor(WebhookDeliveryRepository webhookDeliveryRepository, RestTemplate restTemplate){
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.restTemplate = restTemplate;
    }

    public void processDueDeliveries(){
        List<WebhookDelivery> deliveries = webhookDeliveryRepository
                .findDueDeliveries(WebhookDeliveryStatus.PENDING, LocalDateTime.now());

        for (WebhookDelivery delivery : deliveries){
            try {
                process(delivery);
            } catch (Exception e){
                log.error("Unexpected error processing delivery {}: {}", delivery.getId(), e.getMessage());
            }
        }
    }

    private void process(WebhookDelivery delivery){
        delivery.setAttemptCount(delivery.getAttemptCount());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(delivery.getPayload(), headers);

            restTemplate.postForEntity(delivery.getSubscription().getTargetUrl(), request, String.class);

            delivery.setStatus(WebhookDeliveryStatus.SUCCESS);
            delivery.setLastError(null);
            log.info("Webhook delivery {} succeeded on attemps {}", delivery.getId(), delivery.getAttemptCount());
        } catch (HttpClientErrorException e){
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            delivery.setLastError(truncate("Rejected by receiver: HTTP " + e.getStatusCode().value()));
            log.warn("Webhook delivery {} parmanently  failed: HTTP {}", delivery.getId(), e.getStatusCode().value());
        } catch (RestClientException e){
            scheduleRetryOrFail(delivery, e.getMessage());
        }

        webhookDeliveryRepository.save(delivery);
    }

    private void scheduleRetryOrFail(WebhookDelivery delivery, String error){
        int attempts = delivery.getAttemptCount();
        delivery.setLastError(truncate(error));

        if (attempts >= MAX_ATTEMPTS){
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            log.error("Webhook delivery {} failed after {} attempts, giving up", delivery.getId(), attempts);
        } else {
            long delayMinutes = BACKOFF_MINUTES[attempts - 1];
            delivery.setNextAttemptAt(LocalDateTime.now().plusMinutes(delayMinutes));
            log.warn("Webhook delivery {} failed (attempts {}), retrying in {} min",
                    delivery.getId(), attempts, delayMinutes);
        }
    }

    private String truncate(String text){
        if (text == null){
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}
