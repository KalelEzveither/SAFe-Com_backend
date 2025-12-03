package com.safecom.safe_backend.controller;

import com.safecom.safe_backend.model.Produto;
import com.safecom.safe_backend.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<Produto> list() {
        return produtoService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> get(@PathVariable long id) {
        return produtoService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produto> create(@RequestBody Produto p) {
        Produto created = produtoService.create(p);
        return ResponseEntity.created(URI.create("/api/produtos/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable long id, @RequestBody Produto p) {
        boolean ok = produtoService.update(id, p);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        boolean ok = produtoService.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
