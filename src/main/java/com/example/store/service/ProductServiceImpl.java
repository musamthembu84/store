package com.example.store.service;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.repository.ProductOrder;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductDTO> getAllProducts() {
        return toProductDTOs(productRepository.findAllProductOrderRows());
    }

    @Override
    public ProductDTO getProductById(Long id) {
        List<ProductDTO> products = toProductDTOs(productRepository.findProductOrderRowsById(id));
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        return products.get(0);
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        Product created = productRepository.save(product);
        log.info("Created product id={}", created.getId());
        return created;
    }

    private List<ProductDTO> toProductDTOs(List<ProductOrder> rows) {
        Map<Long, ProductDTO> productsById = new LinkedHashMap<>();
        for (ProductOrder row : rows) {
            ProductDTO productDTO = productsById.computeIfAbsent(row.productId(), id -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(id);
                dto.setDescription(row.description());
                dto.setOrders(new ArrayList<>());
                return dto;
            });
            if (row.orderId() != null) {
                productDTO.getOrders().add(row.orderId());
            }
        }
        return new ArrayList<>(productsById.values());
    }
}
