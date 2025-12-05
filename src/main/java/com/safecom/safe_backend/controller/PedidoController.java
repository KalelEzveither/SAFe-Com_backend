package com.safecom.safe_backend.controller;

import com.safecom.safe_backend.controller.dto.PedidoRequest;
import com.safecom.safe_backend.controller.dto.PedidoResponse;
import com.safecom.safe_backend.dao.JdbcPedidoDao;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final JdbcPedidoDao pedidoDao;

    public PedidoController(JdbcPedidoDao pedidoDao) {
        this.pedidoDao = pedidoDao;
    }

    @PostMapping("/finalizar")
    public ResponseEntity<?> finalizarPedido(@RequestBody PedidoRequest request) {
        String resultado = pedidoDao.criarPedido(request);

        if ("SUCESSO".equals(resultado)) {
            return ResponseEntity.ok("Reserva realizada com sucesso!");
        } else if (resultado.startsWith("ESTOQUE_INSUFICIENTE")) {
            return ResponseEntity.status(409).body(resultado);
        } else if ("CARRINHO_VAZIO".equals(resultado)) {
            return ResponseEntity.badRequest().body("Carrinho vazio.");
        } else {
            return ResponseEntity.internalServerError().body(resultado);
        }
    }
    
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<PedidoResponse>> listarMeusPedidos(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoDao.listarPorComprador(id));
    }

    // GET /api/pedidos/barraca/{id} -> Gestão de Reservas (Vendedor)
    @GetMapping("/barraca/{id}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosBarraca(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoDao.listarPorBarraca(id));
    }

    // PUT /api/pedidos/{id}/status?status=PRONTO -> Mudar status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestParam String status) {
        boolean ok = pedidoDao.atualizarStatus(id, status);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}