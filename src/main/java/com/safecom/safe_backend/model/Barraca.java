package com.safecom.safe_backend.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Barraca {
    private Long id;
    private String nome;
    private String descricao;
    private Boolean isAberta; 
    private String imagemUrl;         // URL da foto
    private String horarioFuncionamento; // Texto simples: "12:00 às 14:00"
    private Long usuarioId;
    // O Front manda: [1, 4] (Hortifruti e Artesanato)
    private List<Integer> categoriaIds = new ArrayList<>();
}
