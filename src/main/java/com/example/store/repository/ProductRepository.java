package com.example.store.repository;

import com.example.store.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * One row per (product, order) pair, or one row with a null order id for a product that belongs to no orders yet.
     * Aggregated into {@code ProductDTO}s in the service layer, so listing every product with its order ids costs
     * exactly one round trip, and we never load a full {@code Order} just to read its id.
     */
    @Query("SELECT new com.example.store.repository.ProductOrder(p.id, p.description, o.id) "
            + "FROM Product p LEFT JOIN p.orders o ORDER BY p.id")
    List<ProductOrder> findAllProductOrderRows();

    @Query("SELECT new com.example.store.repository.ProductOrder(p.id, p.description, o.id) "
            + "FROM Product p LEFT JOIN p.orders o WHERE p.id = :id")
    List<ProductOrder> findProductOrderRowsById(@Param("id") Long id);
}
