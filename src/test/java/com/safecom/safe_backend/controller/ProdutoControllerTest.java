package com.safecom.safe_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecom.safe_backend.model.Produto;
import com.safecom.safe_backend.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProdutoService produtoService;

    @Test
    void listReturnsProdutos() throws Exception {
        Produto p = new Produto(1L, "Caneta", "Caneta azul", new java.math.BigDecimal("1.50"), null, "PAPELARIA", null);
        given(produtoService.list()).willReturn(List.of(p));

        mvc.perform(get("/api/produtos")).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(p))));
    }

    @Test
    void createReturnsCreated() throws Exception {
        Produto req = new Produto(null, "Lápis", "HB", new java.math.BigDecimal("0.50"), null, "PAPELARIA", 1L);
        Produto created = new Produto(2L, "Lápis", "HB", new java.math.BigDecimal("0.50"), null, "PAPELARIA", 1L);
        given(produtoService.create(any())).willReturn(created);

        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }
}
