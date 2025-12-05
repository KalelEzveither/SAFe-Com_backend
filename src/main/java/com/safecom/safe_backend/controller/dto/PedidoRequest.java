package com.safecom.safe_backend.controller.dto;

public class PedidoRequest {
    private Long usuarioId;
    private String metodoPagamento; // "PIX" ou "DINHEIRO"
    private String tipoEntrega;     // "RETIRADA"
    private Double trocoPara;       // Opcional

    // Getters e Setters
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
    public Double getTrocoPara() { return trocoPara; }
    public void setTrocoPara(Double trocoPara) { this.trocoPara = trocoPara; }
}