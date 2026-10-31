package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.WebhookDelivery;
import com.backend.taskflowapi.entity.WebhookSubscription;
import com.backend.taskflowapi.enums.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findByStatusAndNextAttemptAtLessThanEqual(
            WebhookDeliveryStatus status, LocalDateTime time
    );

    void deleteBySubscription(WebhookSubscription subscription);

    @Query("SELECT d FROM WebhookDelivery d JOIN FETCH d.subscription " +
            "WHERE d.status = :status AND d.nextAttemptAt <= :time")
    List<WebhookDelivery> findDueDeliveries(
            @Param("status") WebhookDeliveryStatus status,
            @Param("time") LocalDateTime time
    );
}
