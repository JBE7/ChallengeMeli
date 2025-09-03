package com.example.productcompare.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** Modelo de producto para comparación en el front. */
public final class Product {
    private final long id;
    private final String name;
    private final String imageUrl;
    private final String description;
    private final double price;
    private final double rating;
    private final Map<String, String> specifications;

    @JsonCreator
    public Product(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("description") String description,
            @JsonProperty("price") double price,
            @JsonProperty("rating") double rating,
            @JsonProperty("specifications") Map<String, String> specifications) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.specifications = specifications;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }
}
