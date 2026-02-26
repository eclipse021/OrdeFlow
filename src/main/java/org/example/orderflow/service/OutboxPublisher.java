package org.example.orderflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderflow.config.RabbitConfig;
import org.example.orderflow.dto.OrderEvent;
import org.example.orderflow.entity.EventType;
import org.example.orderflow.entity.Outbox;
import org.example.orderflow.repository.OutboxRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)  // 1초마다 실행
    @Transactional
    public void publishOutboxMessages() {
        List<Outbox> pendingMessages = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (Outbox outbox : pendingMessages) {
            try {
                publishMessage(outbox);
                outbox.markAsProcessed();
                log.info("[OutboxPublisher] 메시지 발행 성공 - outboxId: {}, aggregateId: {}",
                        outbox.getId(), outbox.getAggregateId());
            } catch (Exception e) {
                log.error("[OutboxPublisher] 메시지 발행 실패 - outboxId: {}, error: {}",
                        outbox.getId(), e.getMessage());
                // 실패해도 다음 메시지 처리 계속 (재시도는 다음 스케줄에서)
            }
        }
    }

    private void publishMessage(Outbox outbox) {
        switch (outbox.getEventType()) {
            case ORDER_CREATED -> {
                OrderEvent event = fromJson(outbox.getPayload(), OrderEvent.class);
                rabbitTemplate.convertAndSend(
                        RabbitConfig.ORDER_EXCHANGE,
                        RabbitConfig.ORDER_CREATED_ROUTING_KEY,
                        event
                );
            }
            // 다른 이벤트 타입 추가 시 case 추가
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
