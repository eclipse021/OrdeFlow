package org.example.orderflow.service;

import lombok.extern.slf4j.Slf4j;
import org.example.orderflow.config.RabbitConfig;
import org.example.orderflow.dto.OrderEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleNotification(OrderEvent event) {
        log.info("[Notification] 알림 전송 시작 - orderId: {}, email: {}",
                event.orderId(), event.userEmail());

        // 실제로는 여기서 이메일/푸시 알림 전송 로직 수행
        // 비동기 처리 체감을 위해 1초 대기
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[Notification] 알림 전송 완료 - {}님에게 주문(orderId: {}) 완료 알림 전송",
                event.userEmail(), event.orderId());
    }
}
