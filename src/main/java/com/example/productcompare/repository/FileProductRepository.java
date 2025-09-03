package com.example.productcompare.repository;

import com.example.productcompare.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Repositorio basado en archivo JSON en classpath; sirve datos desde memoria.
 */
@Repository
public class FileProductRepository {
    private final ObjectMapper objectMapper;
    private Map<Long, Product> cachedProducts;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileProductRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Carga products.json en memoria al iniciar el bean. */
    @PostConstruct
    public void init() {
        lock.writeLock().lock();
        try (InputStream is = new ClassPathResource("products.json").getInputStream()) {
            List<Product> productList = objectMapper.readValue(is, new TypeReference<List<Product>>() {});
            cachedProducts = productList.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load products.json", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Retorna todos los productos. */
    public List<Product> findAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(cachedProducts.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Busca un producto por ID o null si no existe. */
    public Product findById(long id) {
        lock.readLock().lock();
        try {
            return cachedProducts.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Devuelve los productos para los IDs dados, preservando el orden de entrada.
     */
    public List<Product> findByIds(List<Long> ids) {
        lock.readLock().lock();
        try {
            return ids.stream()
                    .map(cachedProducts::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}
