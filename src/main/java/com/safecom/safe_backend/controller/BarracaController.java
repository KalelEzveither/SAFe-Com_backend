package com.safecom.safe_backend.controller;

import com.safecom.safe_backend.model.Barraca;
import com.safecom.safe_backend.service.BarracaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/barracas")
public class BarracaController {

    private final BarracaService barracaService;

    public BarracaController(BarracaService barracaService) {
        this.barracaService = barracaService;
    }

    @GetMapping
    public List<Barraca> list() {
        return barracaService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barraca> get(@PathVariable long id) {
        return barracaService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Barraca> create(@RequestBody Barraca b) {
        Barraca created = barracaService.create(b);
        return ResponseEntity.created(URI.create("/api/barracas/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable long id, @RequestBody Barraca b) {
        boolean ok = barracaService.update(id, b);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        boolean ok = barracaService.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
