package com.example.store.service;

import com.example.store.entity.Order;

import java.util.List;

/** Business operations for orders. */
public interface OrderService {

    /** Returns every order, with its customer and products. */
    List<Order> getAllOrders();

    /** Returns a single order by id. */
    Order getOrderById(Long id);

    /** Creates a new order for an existing customer, referencing 1 or more existing products. */
    Order createOrder(Order order);
}
