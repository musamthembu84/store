package com.example.store.service;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.repository.ProductOrder;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void getAllProductsGroupsMultipleOrderRowsIntoOneDtoPerProduct() {
        // product 1 appears in orders 10 and 20; product 2 appears in no orders yet
        when(productRepository.findAllProductOrderRows())
                .thenReturn(List.of(
                        new ProductOrder(1L, "Widget", 10L),
                        new ProductOrder(1L, "Widget", 20L),
                        new ProductOrder(2L, "Gadget", null)));

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getDescription()).isEqualTo("Widget");
        assertThat(result.get(0).getOrders()).containsExactly(10L, 20L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getOrders()).isEmpty();
    }

    @Test
    void getProductByIdReturnsAggregatedOrderIds() {
        when(productRepository.findProductOrderRowsById(1L)).thenReturn(List.of(new ProductOrder(1L, "Widget", 10L)));

        ProductDTO result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrders()).containsExactly(10L);
    }

    @Test
    void getProductByIdThrowsWhenNoRowsReturned() {
        when(productRepository.findProductOrderRowsById(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found: 99");
    }

    @Test
    void createProductDelegatesToRepository() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.createProduct(product);

        assertThat(result).isEqualTo(product);
    }
}
