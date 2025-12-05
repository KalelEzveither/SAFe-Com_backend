package com.safecom.safe_backend.service;

import com.safecom.safe_backend.dao.ProdutoDao;
import com.safecom.safe_backend.model.Produto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoDao produtoDao;

    public ProdutoService(ProdutoDao produtoDao) {
        this.produtoDao = produtoDao;
    }

    public List<Produto> list() {
        return produtoDao.findAll();
    }

    public Optional<Produto> get(long id) {
        return produtoDao.findById(id);
    }

    public Produto create(Produto p) {
        return produtoDao.create(p);
    }

    public boolean update(long id, Produto p) {
        p.setId(id);
        return produtoDao.update(p);
    }

    public boolean delete(long id) {
        return produtoDao.delete(id);
    }

    public List<Produto> listarPorBarraca(long barracaId) {
        return produtoDao.findByBarracaId(barracaId);
    }
}
