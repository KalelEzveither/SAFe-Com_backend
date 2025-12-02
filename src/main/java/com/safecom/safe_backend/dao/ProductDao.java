package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {

    List<Product> findAll();

    Optional<Product> findById(long id);

    Product create(Product product);

    boolean update(Product product);

    boolean delete(long id);

}
