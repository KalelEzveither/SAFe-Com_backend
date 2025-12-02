package com.safecom.safe_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecom.safe_backend.model.Product;
import com.safecom.safe_backend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProductService productService;

    @Test
    void listReturnsProducts() throws Exception {
        Product p = new Product(1L, "Pen", "Blue pen", 1.5, 100);
        given(productService.list()).willReturn(List.of(p));

        mvc.perform(get("/api/products")).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(p))));
    }

    @Test
    void createReturnsCreated() throws Exception {
        Product req = new Product(null, "Pencil", "HB", 0.5, 200);
        Product created = new Product(2L, "Pencil", "HB", 0.5, 200);
        given(productService.create(any())).willReturn(created);

        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }
}
