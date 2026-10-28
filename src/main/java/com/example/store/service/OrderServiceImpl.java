package com.example.store.service;

import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithCustomerAndProducts();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository
                .findByIdWithCustomerAndProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        final Long customerId = order.getCustomer().getId();
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        for (Product product : order.getProducts()) {
            if (!productRepository.existsById(product.getId())) {
                throw new ResourceNotFoundException("Product not found: " + product.getId());
            }
        }
        Order created = orderRepository.save(order);
        log.info("Created order id={} for customer id={}", created.getId(), customerId);
        return created;
    }
}
