package org.example.orderflow.service;

import lombok.extern.slf4j.Slf4j;
import org.example.orderflow.config.RabbitConfig;
import org.example.orderflow.dto.OrderEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StockService {

    @RabbitListener(queues = RabbitConfig.STOCK_QUEUE)
    public void handleStockDeduction(OrderEvent event) {
        log.info("[Stock] 재고 차감 시작 - orderId: {}, productId: {}, quantity: {}",
                event.orderId(), event.productId(), event.quantity());

        // 실제로는 여기서 재고 차감 로직 수행
        // 비동기 처리 체감을 위해 2초 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[Stock] 재고 차감 완료 - orderId: {}, productId: {}", event.orderId(), event.productId());
    }
}
