package com.safecom.safe_backend.controller;

import com.safecom.safe_backend.controller.dto.LoginRequest;
import com.safecom.safe_backend.controller.dto.RegisterRequest;
import com.safecom.safe_backend.model.Usuario;
import com.safecom.safe_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @SuppressWarnings("null")
    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody RegisterRequest req) {
        Usuario u = new Usuario();
        u.setNome(req.nome);
        u.setEmail(req.email);
        u.setSenha(req.senha);
        u.setCpfCnpj(req.cpfCnpj);
        u.setTelefone(req.telefone);
        u.setTipo(req.tipo);
        Usuario created = authService.register(u);
        // Do not expose hashed password in responses
        created.setSenha(null);
        return ResponseEntity.created(URI.create("/api/auth/users/" + created.getId())).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest req) {
        return authService.login(req.email, req.senha)
                .map(u -> {
                    u.setSenha(null); // don't return password
                    return ResponseEntity.ok(u);
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/feirante")
    public ResponseEntity<Void> registerFeirante(@RequestBody com.safecom.safe_backend.controller.dto.FeiranteRequest req) {
        authService.registrarFeiranteCompleto(req.usuario, req.barraca);
        return ResponseEntity.ok().build();
    }
}
