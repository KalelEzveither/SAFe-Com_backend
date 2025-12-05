package com.safecom.safe_backend.model;

import lombok.Data;

@Data
public class ItemCarrinho {
    private Long id;
    private Long usuarioId;
    private Long produtoId;
    private int quantidade;
    
    // Campos "transientes" (apenas para leitura/exibição no front, não salva no banco)
    private String nomeProduto;
    private Double precoUnitario;
    private String imagemUrl;
    private Long barracaId;
    private String nomeBarraca;
}