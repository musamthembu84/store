package com.example.store.service;

import com.example.store.entity.Customer;

import java.util.List;

/** Business operations for customers. */
public interface CustomerService {

    /** Returns every customer, with their orders. */
    List<Customer> getAllCustomers();

    /** Returns customers where at least one word in their name contains the given substring. */
    List<Customer> searchCustomersByName(String query);

    /** Creates a new customer. */
    Customer createCustomer(Customer customer);
}
