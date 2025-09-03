package com.example.productcompare.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.productcompare.exception.NotFoundException;
import com.example.productcompare.model.Product;
import com.example.productcompare.service.ProductService;

/**
 * REST Controller: - GET /api/products - GET /api/products/{id} - GET
 * /api/products/compare?ids=1,2,3
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /**
     * Lista completa de productos.
     */
    @GetMapping
    public ResponseEntity<List<Product>> all() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    /**
     * Producto por ID (404 si no existe).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> byId(@PathVariable("id") long id) {
        Product p = service.getProductById(id);
        if (p == null) {
            throw new NotFoundException("Product with id " + id + " not found");
        }
        return ResponseEntity.ok(p);
    }

    /**
     * Retorna productos para comparar en el mismo orden que 'ids'.
     */
    @GetMapping("/compare")
    public ResponseEntity<List<Product>> compare(@RequestParam("ids") String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(x -> x != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(service.getProductsForCompare(idList));
    }
}
