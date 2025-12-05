package com.safecom.safe_backend.controller;

import com.safecom.safe_backend.dao.JdbcCarrinhoDao;
import com.safecom.safe_backend.model.ItemCarrinho;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoController {

    private final JdbcCarrinhoDao carrinhoDao;

    public CarrinhoController(JdbcCarrinhoDao carrinhoDao) {
        this.carrinhoDao = carrinhoDao;
    }

    // Listar itens do usuário: GET /api/carrinho/usuario/{id}
    @GetMapping("/usuario/{usuarioId}")
    public List<ItemCarrinho> listar(@PathVariable Long usuarioId) {
        return carrinhoDao.listarPorUsuario(usuarioId);
    }

    // Adicionar: POST /api/carrinho
    // Body: { "usuarioId": 1, "produtoId": 5, "quantidade": 2 }
    @PostMapping
    public ResponseEntity<?> adicionar(@RequestBody ItemCarrinho item) {
        String resultado = carrinhoDao.adicionarItem(item.getUsuarioId(), item.getProdutoId(), item.getQuantidade());
        
        if ("ERRO_BARRACA_DIFERENTE".equals(resultado)) {
            return ResponseEntity.status(409).body("Conflito: Você possui itens de outra barraca no carrinho.");
        }
        return ResponseEntity.ok().build();
    }

    // Remover item: DELETE /api/carrinho/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        carrinhoDao.removerItem(id);
        return ResponseEntity.noContent().build();
    }
    
    // Limpar tudo: DELETE /api/carrinho/usuario/{id}
    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> limpar(@PathVariable Long usuarioId) {
        carrinhoDao.limparCarrinho(usuarioId);
        return ResponseEntity.noContent().build();
    }
}