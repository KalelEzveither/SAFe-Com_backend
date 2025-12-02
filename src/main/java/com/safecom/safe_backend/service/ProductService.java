package com.safecom.safe_backend.service;

import com.safecom.safe_backend.dao.ProductDao;
import com.safecom.safe_backend.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> list() {
        return productDao.findAll();
    }

    public Optional<Product> get(long id) {
        return productDao.findById(id);
    }

    public Product create(Product p) {
        return productDao.create(p);
    }

    public boolean update(long id, Product p) {
        p.setId(id);
        return productDao.update(p);
    }

    public boolean delete(long id) {
        return productDao.delete(id);
    }
}
