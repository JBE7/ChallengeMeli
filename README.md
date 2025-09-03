# Product Compare API — Java + Spring Boot

API backend simplificada para un feature de comparación de productos. Provee endpoints para listar, obtener por ID y comparar múltiples ítems. Persistencia en **JSON local** (sin base de datos, pero facilmente escalable).

## Arquitectura

```
┌───────────────────────────────────────────────────────────────┐
│                       Spring Boot App                         │
│                                                               │
│  Controller (REST)  →  Service (negocio)  →  Repository (I/O) │
│       ↑                             ↓                         │
│  GlobalExceptionHandler         products.json (classpath)     │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

- **Controller**: expone endpoints REST (`/api/products`), valida y parsea inputs
- **Service**: orquesta reglas simples y delega acceso a datos al repositorio
- **Repository**: carga `products.json` al iniciar
- **GlobalExceptionHandler**: manejo de errores (404/500)
- **Static**: incluye `favicon.ico` para evitar warnings comunes del navegador

## Endpoints

- `GET /api/products` — Lista todo el catálogo
- `GET /api/products/{id}` — Producto por ID (404 si no existe)
- `GET /api/products/compare?ids=1,2,3` — Devuelve los productos en el **mismo orden** que `ids`

### Ejemplos
```bash
curl http://localhost:8080/api/products | jq
curl http://localhost:8080/api/products/1 | jq
curl "http://localhost:8080/api/products/compare?ids=2,3,5" | jq
```

## Cómo ejecutar

Requisitos: **Java 17** y **Maven 3.9+**

```bash
mvn clean package
mvn spring-boot:run
# Expone en http://localhost:8080
```

## Tests 

- **Service tests** (`ProductServiceTest`): mock del repositorio para validar que la capa de negocio delega correctamente y que `compare()` mantiene el **orden de entrada**. Esto asegura que el front reciba exactamente los productos en el orden que el usuario solicitó.
- **Controller tests** (`ProductControllerTest`): prueban con `MockMvc` que los endpoints principales respondan **200** y contenido **JSON**, validando el wiring de Spring Boot y el mapeo de rutas.

Ejecutar tests:
```bash
mvn -q -Dtest=*Test test
```

## Explicación del código

- `ProductCompareApplication`: clase principal que arranca Spring Boot.
- `Product`: modelo con `name`, `image_url`, `description`, `price`, `rating` y `specifications`.
- `FileProductRepository`: carga `products.json` en memoria (thread-safe con `ReadWriteLock`). Ofrece `findAll`, `findById`, `findByIds`.
- `ProductService`: capa de negocio. Expone `getAllProducts`, `getProductById`, `getProductsForCompare`.
- `ProductController`: endpoints REST documentados, con nombres de parámetros explícitos.
- `GlobalExceptionHandler`: formato de error consistente para 404 y 500.
- `products.json`: 14 productos mock (imágenes placeholder estables por seed).

## Decisiones de diseño

- Proyecto (API Rest) para almacenar y devolver una lista de productos (con sus atributos ya definidos), que se desean comparar
- Tecnología elegida: JAVA + Spring Boot (adaptable fácilmente a Kotlin)
- Persistencia en .jsons (Jackson)
- Tests unitarios (Mockito)
- Manejo de errores (Exceptions)

## Extras de la solución
- Le pedí a ChatGPT que me ayudara en la generación de la documentación (diagramas) y que realice sugerencias sobre el código
- También que generara un set de datos de pruebas basado en productos que podría encontrar dentro de la página de Mercado Libre
- Investigué la integración con la API de Developers de Mercado Libre para traer productos, pero por cuestiones de tiempo lo dejé estructurado para un segundo MVP (MercadoLibreClient.java)

## Mejoras Realizadas (Sept 2025)

Se realizó una refactorización y mejora integral del código, con los siguientes cambios clave:

*   **Rendimiento:** Se optimizó el `FileProductRepository` para usar un `Map` en lugar de una `List` para el caché de productos, mejorando la velocidad de búsqueda de O(n) a O(1).
*   **Inmutabilidad:** El modelo `Product` ahora es inmutable, una buena práctica que aumenta la seguridad en entornos multi-hilo y la predictibilidad del código.
*   **Manejo de Errores:** Se mejoró la validación en el endpoint `/compare` para que devuelva un error 400 (Bad Request) si se proveen IDs inválidos. Además, la lógica de "producto no encontrado" se movió a la capa de servicio, mejorando la separación de responsabilidades.
*   **Calidad de Código:** Se corrigió un bug sutil en el manejo de excepciones con streams de Java, reemplazándolo por un bucle `for` más claro y robusto.
