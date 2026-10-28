package com.example.store.controller;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.mapper.CustomerMapper;
import com.example.store.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setDescription("Test Product");

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Test Product");
        productDTO.setOrders(List.of(10L, 20L));
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.createProduct(product)).thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Product"))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders").isEmpty());
    }

    @Test
    void testCreateProductBlankDescriptionIsBadRequest() throws Exception {
        product.setDescription(" ");

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("description: description must not be blank"));
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test Product"))
                .andExpect(jsonPath("$[0].orders[0]").value(10))
                .andExpect(jsonPath("$[0].orders[1]").value(20));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Product"))
                .andExpect(jsonPath("$.orders[0]").value(10))
                .andExpect(jsonPath("$.orders[1]").value(20));
    }

    @Test
    void testGetProductByIdNotFound() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ResourceNotFoundException("Product not found: 99"));

        mockMvc.perform(get("/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found: 99"));
    }
}
