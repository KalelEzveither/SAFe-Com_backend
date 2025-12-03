package com.safecom.safe_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecom.safe_backend.model.Barraca;
import com.safecom.safe_backend.service.BarracaService;
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

@WebMvcTest(BarracaController.class)
class BarracaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BarracaService barracaService;

    @Test
    void listReturnsBarracas() throws Exception {
        Barraca b = new Barraca(1L, "Loja A", "Descrição", 1L);
        given(barracaService.list()).willReturn(List.of(b));

        mvc.perform(get("/api/barracas")).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(b))));
    }

    @Test
    void createReturnsCreated() throws Exception {
        Barraca req = new Barraca(null, "Minha Barraca", "Boa comida", 2L);
        Barraca created = new Barraca(2L, "Minha Barraca", "Boa comida", 2L);
        given(barracaService.create(any())).willReturn(created);

        mvc.perform(post("/api/barracas").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }
}
