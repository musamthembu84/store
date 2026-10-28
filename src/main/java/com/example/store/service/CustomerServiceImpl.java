package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllWithOrders();
    }

    @Override
    public List<Customer> searchCustomersByName(String query) {
        log.debug("Searching customers by name query: {}", query);
        List<Long> ids = customerRepository.findIdsByNameWordContaining(escapeLikePattern(query));
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return customerRepository.findByIdInWithOrders(ids);
    }

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {
        Customer created = customerRepository.save(customer);
        log.info("Created customer id={}", created.getId());
        return created;
    }

    private String escapeLikePattern(String query) {
        return query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
