package org.example.orderflow.dto;

public record OrderEvent(
        Long orderId,
        Long productId,
        int quantity,
        String userEmail
) {
}
