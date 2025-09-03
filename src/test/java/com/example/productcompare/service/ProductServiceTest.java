package com.example.productcompare.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import com.example.productcompare.model.Product;
import com.example.productcompare.repository.FileProductRepository;

/** Tests unitarios para ProductService. */
public class ProductServiceTest {

    private FileProductRepository repo;
    private ProductService service;

    @BeforeEach
    public void setup() {
        repo = Mockito.mock(FileProductRepository.class);
        Product p1 = new Product();
        p1.setId(1);
        p1.setName("A");
        Product p2 = new Product();
        p2.setId(2);
        p2.setName("B");
        when(repo.findAll()).thenReturn(List.of(p1, p2));
        when(repo.findById(1L)).thenReturn(p1);
        when(repo.findByIds(List.of(2L, 1L))).thenReturn(List.of(p2, p1));
        service = new ProductService(repo);
    }

    @Test
    public void allProducts() {
        List<Product> all = service.getAllProducts();
        assertThat(all).hasSize(2);
    }

    @Test
    public void getById() {
        Product p = service.getProductById(1);
        assertThat(p).isNotNull();
        assertThat(p.getName()).isEqualTo("A");
    }

    @Test
    public void compareKeepsOrder() {
        List<Product> comp = service.getProductsForCompare(List.of(2L, 1L));
        assertThat(comp.get(0).getId()).isEqualTo(2L);
        assertThat(comp.get(1).getId()).isEqualTo(1L);
    }
}
