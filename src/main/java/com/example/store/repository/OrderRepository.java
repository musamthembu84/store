package com.example.store.repository;

import com.example.store.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Fetch-joins {@code customer} (to-one, no duplication) and {@code products} (to-many, hence the DISTINCT to
     * collapse the duplicated root rows back into one {@code Order} per id) so both are populated in this single query
     * instead of triggering lazy loads once the mapper touches them.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer LEFT JOIN FETCH o.products")
    List<Order> findAllWithCustomerAndProducts();

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer LEFT JOIN FETCH o.products WHERE o.id = :id")
    Optional<Order> findByIdWithCustomerAndProducts(@Param("id") Long id);
}
