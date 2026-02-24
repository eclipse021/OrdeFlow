package org.example.orderflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderflow.config.RabbitConfig;
import org.example.orderflow.dto.OrderEvent;
import org.example.orderflow.dto.OrderRequest;
import org.example.orderflow.entity.Order;
import org.example.orderflow.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

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

        // 2. 메시지 발행 (비동기 전송)
        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getUserEmail()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                RabbitConfig.ORDER_CREATED_ROUTING_KEY,
                event
        );
        log.info("[Order] 메시지 발행 완료 - orderId: {}", savedOrder.getId());

        return savedOrder;
    }
}
