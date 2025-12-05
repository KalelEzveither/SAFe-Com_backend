package com.safecom.safe_backend.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PedidoResponse {
    private Long id;
    private LocalDateTime dataPedido;
    private String status;
    private String tipoEntrega;
    private BigDecimal valorTotal;
    private String nomeOutraParte; // Se sou cliente, vejo nome da Barraca. Se sou vendedor, nome do Cliente.
    private String resumoItens;    // Ex: "1x Pastel de Carne, 2x Caldo de Cana"
}