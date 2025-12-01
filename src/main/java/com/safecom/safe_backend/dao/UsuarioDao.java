package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Usuario;

import java.util.Optional;

public interface UsuarioDao {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findById(long id);
    Usuario create(Usuario usuario);
}
