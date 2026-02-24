package org.example.orderflow.dto;

public record OrderRequest(
        Long productId,
        int quantity,
        String userEmail
) {
}
