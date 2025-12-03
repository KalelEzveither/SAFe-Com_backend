package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoDao {

    List<Produto> findAll();

    Optional<Produto> findById(long id);

    Produto create(Produto produto);

    boolean update(Produto produto);

    boolean delete(long id);

    boolean baixarEstoque(long produtoId, int quantidadeComprada);
}
