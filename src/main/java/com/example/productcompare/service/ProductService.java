package com.example.productcompare.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.productcompare.model.Product;
import com.example.productcompare.repository.FileProductRepository;

/** Capa de negocio: delega en repo, escalable a db. */
@Service
public class ProductService {
    private final FileProductRepository repository;

    public ProductService(FileProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product getProductById(long id) {
        return repository.findById(id);
    }

    public List<Product> getProductsForCompare(List<Long> ids) {
        return repository.findByIds(ids);
    }
}
