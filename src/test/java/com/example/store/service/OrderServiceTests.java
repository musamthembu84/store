package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, customerRepository, productRepository);
    }

    @Test
    void getAllOrdersFetchJoinsCustomerAndProductsToAvoidLazyLoadRoundTrips() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findAllWithCustomerAndProducts()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).containsExactly(order);
        verify(orderRepository).findAllWithCustomerAndProducts();
    }

    @Test
    void getOrderByIdFetchJoinsCustomerAndProductsToAvoidLazyLoadRoundTrips() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findByIdWithCustomerAndProducts(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isEqualTo(order);
        verify(orderRepository).findByIdWithCustomerAndProducts(1L);
    }

    @Test
    void getOrderByIdThrowsWhenMissing() {
        when(orderRepository.findByIdWithCustomerAndProducts(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found: 99");
    }

    @Test
    void createOrderSavesWhenCustomerAndProductsExist() {
        Customer customer = new Customer();
        customer.setId(1L);
        Product product = new Product();
        product.setId(1L);
        Order order = new Order();
        order.setCustomer(customer);
        order.setProducts(List.of(product));

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertThat(result).isEqualTo(order);
    }

    @Test
    void createOrderThrowsWhenCustomerMissing() {
        Customer customer = new Customer();
        customer.setId(99L);
        Order order = new Order();
        order.setCustomer(customer);
        order.setProducts(List.of());

        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found: 99");
    }

    @Test
    void createOrderThrowsWhenProductMissing() {
        Customer customer = new Customer();
        customer.setId(1L);
        Product product = new Product();
        product.setId(99L);
        Order order = new Order();
        order.setCustomer(customer);
        order.setProducts(List.of(product));

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found: 99");
    }
}
