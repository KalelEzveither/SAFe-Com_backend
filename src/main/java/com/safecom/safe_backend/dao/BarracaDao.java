package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Barraca;

import java.util.List;
import java.util.Optional;

public interface BarracaDao {

    List<Barraca> findAll();

    Optional<Barraca> findById(long id);

    Optional<Barraca> findByUsuarioId(long usuarioId);

    Barraca create(Barraca barraca);

    boolean update(Barraca barraca);

    boolean delete(long id);
}
