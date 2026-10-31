package com.backend.taskflowapi.entity;

import com.backend.taskflowapi.enums.WebhookDeliveryStatus;
import com.backend.taskflowapi.enums.WebhookEvenType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_deliveries")
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private WebhookSubscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookEvenType evenType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookDeliveryStatus status;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public WebhookDelivery(){}

    public WebhookDelivery(WebhookSubscription subscription, WebhookEvenType evenType, String payload){
        this.subscription = subscription;
        this.evenType = evenType;
        this.payload = payload;
        this.status = WebhookDeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public WebhookSubscription getSubscription() {
        return subscription;
    }

    public WebhookEvenType getEvenType() {
        return evenType;
    }

    public String getPayload() {
        return payload;
    }

    public WebhookDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(WebhookDeliveryStatus status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
