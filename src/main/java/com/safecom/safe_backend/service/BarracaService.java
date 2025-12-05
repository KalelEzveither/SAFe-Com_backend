package com.safecom.safe_backend.service;

import com.safecom.safe_backend.dao.BarracaDao;
import com.safecom.safe_backend.dao.UsuarioDao;
import com.safecom.safe_backend.model.Barraca;
import com.safecom.safe_backend.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BarracaService {

    private final BarracaDao barracaDao;
    private final UsuarioDao usuarioDao;

    public BarracaService(BarracaDao barracaDao, UsuarioDao usuarioDao) {
        this.barracaDao = barracaDao;
        this.usuarioDao = usuarioDao;
    }

    public List<Barraca> list() {
        return barracaDao.findAll();
    }

    public Optional<Barraca> get(long id) {
        return barracaDao.findById(id);
    }

    public Barraca create(Barraca b) {
        // Verify owner exists and is a VENDEDOR
        Optional<Usuario> owner = usuarioDao.findById(b.getUsuarioId());
        if (owner.isEmpty()) throw new RuntimeException("Usuario not found");
        if (!"VENDEDOR".equalsIgnoreCase(owner.get().getTipo())) throw new RuntimeException("Usuario is not a VENDEDOR");
        return barracaDao.create(b);
    }

    public boolean update(long id, Barraca b) {
        b.setId(id);
        return barracaDao.update(b);
    }

    public boolean delete(long id) {
        return barracaDao.delete(id);
    }

    public Optional<Barraca> getByUserId(long id) {
        return barracaDao.findByUsuarioId(id);
    }
}
