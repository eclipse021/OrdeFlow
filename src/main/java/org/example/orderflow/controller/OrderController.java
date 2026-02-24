package org.example.orderflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderflow.dto.OrderRequest;
import org.example.orderflow.entity.Order;
import org.example.orderflow.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest request) {
        log.info("[Controller] 주문 요청 수신 - productId: {}, quantity: {}, email: {}",
                request.productId(), request.quantity(), request.userEmail());

        long startTime = System.currentTimeMillis();

        Order order = orderService.createOrder(request);

        long endTime = System.currentTimeMillis();
        log.info("[Controller] 주문 API 응답 완료 - 소요시간: {}ms", endTime - startTime);

        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus(),
                "message", "주문이 접수되었습니다. 재고 차감 및 알림은 비동기로 처리됩니다.",
                "responseTimeMs", endTime - startTime
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "order-service"
        ));
    }
}
