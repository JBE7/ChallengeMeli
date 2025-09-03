package com.example.productcompare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.productcompare.exception.NotFoundException;
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
        return Optional.ofNullable(repository.findById(id))
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));
    }

    public List<Product> getProductsForCompare(List<Long> ids) {
        return repository.findByIds(ids);
    }
}
