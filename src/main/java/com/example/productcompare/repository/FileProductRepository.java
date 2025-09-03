package com.example.productcompare.repository;

import com.example.productcompare.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Repositorio basado en archivo JSON en classpath; sirve datos desde memoria.
 */
@Repository
public class FileProductRepository {
    private final ObjectMapper objectMapper;
    private List<Product> cachedProducts;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileProductRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Carga products.json en memoria al iniciar el bean. */
    @PostConstruct
    public void init() {
        lock.writeLock().lock();
        try (InputStream is = new ClassPathResource("products.json").getInputStream()) {
            cachedProducts = objectMapper.readValue(is, new TypeReference<List<Product>>() {
            });
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
            return List.copyOf(cachedProducts);
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Busca un producto por ID o null si no existe. */
    public Product findById(long id) {
        lock.readLock().lock();
        try {
            return cachedProducts.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
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
                    .map(id -> cachedProducts.stream().filter(p -> p.getId() == id).findFirst().orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}
