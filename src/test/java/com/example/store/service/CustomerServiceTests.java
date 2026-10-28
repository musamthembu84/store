package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void getAllCustomersFetchJoinsOrdersToAvoidLazyLoadRoundTrips() {
        Customer customer = new Customer();
        customer.setId(1L);
        when(customerRepository.findAllWithOrders()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).containsExactly(customer);
        verify(customerRepository).findAllWithOrders();
    }

    @Test
    void escapesPercentWildcardBeforeQueryingRepository() {
        when(customerRepository.findIdsByNameWordContaining("50\\%")).thenReturn(List.of());

        customerService.searchCustomersByName("50%");

        verify(customerRepository).findIdsByNameWordContaining("50\\%");
    }

    @Test
    void escapesUnderscoreAndBackslashBeforeQueryingRepository() {
        when(customerRepository.findIdsByNameWordContaining("a\\_b\\\\c")).thenReturn(List.of());

        customerService.searchCustomersByName("a_b\\c");

        verify(customerRepository).findIdsByNameWordContaining("a\\_b\\\\c");
    }

    @Test
    void searchSkipsHydrationQueryWhenNoIdsMatch() {
        when(customerRepository.findIdsByNameWordContaining("zzz")).thenReturn(List.of());

        List<Customer> result = customerService.searchCustomersByName("zzz");

        assertThat(result).isEmpty();
        verify(customerRepository, never()).findByIdInWithOrders(anyList());
    }

    @Test
    void searchFetchJoinsOrdersForMatchingIdsOnly() {
        Customer customer = new Customer();
        customer.setId(1L);
        when(customerRepository.findIdsByNameWordContaining("oh")).thenReturn(List.of(1L));
        when(customerRepository.findByIdInWithOrders(List.of(1L))).thenReturn(List.of(customer));

        List<Customer> result = customerService.searchCustomersByName("oh");

        assertThat(result).containsExactly(customer);
    }
}
