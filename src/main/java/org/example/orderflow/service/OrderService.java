package org.example.orderflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderflow.dto.OrderEvent;
import org.example.orderflow.dto.OrderRequest;
import org.example.orderflow.entity.AggregateType;
import org.example.orderflow.entity.EventType;
import org.example.orderflow.entity.Order;
import org.example.orderflow.entity.Outbox;
import org.example.orderflow.repository.OrderRepository;
import org.example.orderflow.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. 주문 저장
        Order order = Order.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .userEmail(request.userEmail())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("[Order] 주문 저장 완료 - orderId: {}, productId: {}", savedOrder.getId(), savedOrder.getProductId());

        // 2. Outbox 테이블에 이벤트 저장 (같은 트랜잭션!)
        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getUserEmail()
        );

        Outbox outbox = Outbox.builder()
                .aggregateType(AggregateType.ORDER)
                .aggregateId(savedOrder.getId())
                .eventType(EventType.ORDER_CREATED)
                .payload(toJson(event))
                .build();

        outboxRepository.save(outbox);
        log.info("[Order] Outbox 저장 완료 - orderId: {}, outboxId: {}", savedOrder.getId(), outbox.getId());

        // 이제 RabbitMQ로 직접 발행하지 않음!
        // 별도의 OutboxPublisher가 폴링해서 발행함

        return savedOrder;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
