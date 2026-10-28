package com.example.store.service;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;

import java.util.List;

/** Business operations for products. */
public interface ProductService {

    /** Returns every product, each with the ids of the orders that contain it. */
    List<ProductDTO> getAllProducts();

    /** Returns a single product by id, with the ids of the orders that contain it. */
    ProductDTO getProductById(Long id);

    /** Creates a new product. */
    Product createProduct(Product product);
}
