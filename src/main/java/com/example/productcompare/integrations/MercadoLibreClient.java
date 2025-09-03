package com.example.productcompare.integrations;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.productcompare.model.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Cliente mínimo para consumir la API pública de MercadoLibre (sitio MLA).
 * - Busca items (search) y construye lista de Products ordenada por sold_quantity.
 * - Cache sencillo en memoria (TTL configurable).
 *
 * Referencias: Items & Searches / Best Sellers / Pictures (MercadoLibre Developers).
 */
@Component
public class MercadoLibreClient {

    private final WebClient webClient;
    private final AtomicReference<CachedProducts> cache = new AtomicReference<>(null);
    private final Duration cacheTtl = Duration.ofMinutes(30); // ajustar según necesidades

    public MercadoLibreClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.mercadolibre.com")
                .build();
    }

    /**
     * Obtiene los top N productos (más vendidos) según search+orden local por sold_quantity.
     * @param n cantidad deseada
     * @param query consulta (puede ser categoría, marca o vacío para resultados generales)
     */
    public List<Product> fetchTopNSellers(int n, String query) {
        // Reuso cache si es válido
        CachedProducts cached = cache.get();
        if (cached != null && !cached.isExpired()) {
            return cached.products.stream().limit(n).collect(Collectors.toList());
        }

        // Realizamos búsqueda pública (limit alto para tener margen)
        String searchPath = "/sites/MLA/search?limit=50";
        if (query != null && !query.isBlank()) {
            searchPath += "&q=" + urlEncode(query);
        }

        try {
            // Hacemos una sola llamada síncrona (WebClient Mono.block) pero en scheduler para no bloquear event-loop en uso real
            Mono<SearchResponse> mono = webClient.get()
                    .uri(searchPath)
                    .retrieve()
                    .bodyToMono(SearchResponse.class)
                    .timeout(Duration.ofSeconds(10));

            SearchResponse resp = mono.publishOn(Schedulers.boundedElastic()).block();

            if (resp == null || resp.results == null) return Collections.emptyList();

            // ordenar por sold_quantity desc y mapear a Product
            List<Product> products = resp.results.stream()
                    .sorted(Comparator.comparingInt((MLResult r) -> r.soldQuantity == null ? 0 : r.soldQuantity).reversed())
                    .limit(n)
                    .map(this::mapToProductFromSearchResult)
                    .collect(Collectors.toList());

            // actualizar cache
            cache.set(new CachedProducts(products, System.currentTimeMillis() + cacheTtl.toMillis()));

            return products;

        } catch (WebClientResponseException e) {
            // manejar 429/403...
            throw new RuntimeException("MercadoLibre API error: " + e.getRawStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch from MercadoLibre", e);
        }
    }

    private Product mapToProductFromSearchResult(MLResult r) {
        Product p = new Product();
        p.setId(generateLocalIdFrom(r.id));
        p.setName(r.title != null ? r.title : "Producto ML");
        p.setImageUrl(r.thumbnail); // thumbnail viene en el search result y suele ser URL directa a imagen
        p.setDescription(""); // el search no trae descripción larga; podrías llamar /items/{id} si la necesitás
        p.setPrice(r.price != null ? r.price : 0.0);
        p.setRating(0.0);
        Map<String,String> specs = new HashMap<>();
        specs.put("permalink", r.permalink != null ? r.permalink : "");
        specs.put("sold_quantity", String.valueOf(r.soldQuantity == null ? 0 : r.soldQuantity));
        p.setSpecifications(specs);
        return p;
    }

    private long generateLocalIdFrom(String mlId) {
        // Simple mapping: hashCode positivo
        return Math.abs(Objects.hashCode(mlId));
    }

    private String urlEncode(String s) {
        return s.replace(" ", "%20");
    }

    // --- DTOs mínimos para parsear response (solo campos usados)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchResponse {
        public List<MLResult> results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MLResult {
        public String id;
        public String title;
        public Double price;
        public String thumbnail; // URL de imagen en miniatura
        @JsonProperty("sold_quantity")
        public Integer soldQuantity;
        public String permalink;
    }

    // --- Cache simple
    static class CachedProducts {
        final List<Product> products;
        final long expiresAt;
        CachedProducts(List<Product> products, long expiresAt) {
            this.products = products;
            this.expiresAt = expiresAt;
        }
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}

