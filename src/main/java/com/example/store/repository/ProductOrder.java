package com.example.store.repository;

/** One (product, order) pair, or an order-less product with a null {@code orderId}. */
public record ProductOrder(Long productId, String description, Long orderId) {}
