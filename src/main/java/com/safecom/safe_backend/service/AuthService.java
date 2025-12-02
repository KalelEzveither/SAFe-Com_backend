package com.safecom.safe_backend.service;

import com.safecom.safe_backend.dao.UsuarioDao;
import com.safecom.safe_backend.model.Usuario;
import com.safecom.safe_backend.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioDao usuarioDao;

    public AuthService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public Usuario register(Usuario usuario) {
        // hash password before saving
        usuario.setSenha(PasswordUtil.hash(usuario.getSenha()));
        // default tipo
        if (usuario.getTipo() == null) usuario.setTipo("CLIENTE");
        return usuarioDao.create(usuario);
    }

    public Optional<Usuario> login(String email, String password) {
        Optional<Usuario> found = usuarioDao.findByEmail(email);
        if (found.isEmpty()) return Optional.empty();
        Usuario u = found.get();
        String hashed = PasswordUtil.hash(password);
        if (u.getSenha() != null && u.getSenha().equals(hashed)) {
            return Optional.of(u);
        }
        return Optional.empty();
    }
}
