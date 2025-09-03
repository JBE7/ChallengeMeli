package com.example.productcompare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada: levanta la app y expone los endpoints REST. */
@SpringBootApplication
public class ProductCompareApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductCompareApplication.class, args);
    }
}
