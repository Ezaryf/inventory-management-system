# Inventory Management System - Project Implementation Report

This document maps the implemented codebase to the requirements outlined in the **Inventory Management System Plan**. Each section corresponds to an exercise from the plan, demonstrating how the requirements were met with code examples and explanations.

---

## Exercise 1: Inventory Database & Spring Boot Setup

**Requirement:** Create MySQL database (`products`, `categories`, `suppliers`, `transactions`) and setup Spring Boot with JPA/Lombok.

### Implementation Logic

Instead of writing raw SQL scripts, we utilized **JPA (Java Persistence API)** to define our database schema directly in Java. When the application starts, Hibernate automatically translates these logical entities into MySQL tables.

### Code Evidence

**1. Product Entity (`Product.java`)**
_Maps to the `products` table._

```java
@Entity
@Table(name = "products")
@Data // Lombok: Generates Getters, Setters, toString, etc.
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY) // Relationship to Category
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Integer currentStock;

    // ... verification of cost, reorder level, etc.
}
```

**2. Maven Dependencies (`pom.xml`)**
_Sets up Spring Boot, Database Driver, and Utilities._

```xml
<dependencies>
    <!-- Core Web Framework -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- Database Access (JPA) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    <!-- Lombok (Boilerplate reduction) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## Exercise 2: Product & Category Management API

**Requirement:** Implement CRUD (Create, Read, Update, Delete) for Products and Categories with validation.

### Implementation Logic

We followed the standard **Controller-Service-Repository** architecture.

1.  **Repository**: Handles raw database SQL.
2.  **Service**: Handles business logic (e.g., checking if SKU exists).
3.  **Controller**: Handles HTTP requests and responses.

### Code Evidence

**1. Product Controller (`ProductController.java`)**
_Exposes the endpoints._

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    // GET /api/products (Read all with pagination)
    @GetMapping
    public ResponseEntity<PagedResponse<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findAll(PageRequest.of(page, size)));
    }

    // POST /api/products (Create new)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Security check
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        return new ResponseEntity<>(productService.create(dto), HttpStatus.CREATED);
    }

    // ... PUT and DELETE implementation
}
```

**2. Input Validation (`ProductCreateDTO.java`)**
_Ensures data quality before processing._

```java
public class ProductCreateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "SKU is required")
    private String sku;

    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal unitPrice;
}
```

---

## Exercise 3: Complete Inventory Operations

**Requirement:** Stock IN/OUT operations, Low stock detection, Relationships.

### Implementation Logic

We created a dedicated `InventoryService` to handle stock movements. This ensures that every time stock changes, a **Transaction Record** is created automatically for audit trails.

### Code Evidence

**1. Stock Movement Logic (`InventoryServiceImpl.java`)**

```java
@Override
@Transactional
public ProductDTO removeStock(StockAdjustmentDTO dto) {
    Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

    // Logic: Validation
    if (product.getCurrentStock() < dto.getQuantity()) {
        throw new InsufficientStockException(dto.getProductId(), dto.getQuantity(), product.getCurrentStock());
    }

    // Logic: Update Stock
    product.setCurrentStock(product.getCurrentStock() - dto.getQuantity());

    // Logic: Create History Record
    InventoryTransaction transaction = InventoryTransaction.builder()
            .product(product)
            .transactionType(TransactionType.STOCK_OUT) // Enum: STOCK_IN / STOCK_OUT
            .quantity(dto.getQuantity())
            .build();

    transactionRepository.save(transaction);

    return mapToProductDTO(productRepository.save(product));
}
```

---

## Exercise 4: Refactored Code with SOLID Principles

**Requirement:** SRP (Single Responsibility), ISP (Interface Segregation), DIP (Dependency Injection).

### Implementation Logic

- **SRP**: We split `ProductService` (CRUD) from `InventoryService` (Stock Logic).
- **ISP**: We defined `InventoryOperations` interface to expose only necessary methods.
- **DIP**: Services depend on abstractions (Interfaces) and are injected via Constructor.

### Code Evidence

**1. Interface Segregation (`InventoryOperations.java`)**

```java
// Interface defines EXACTLY what operations are supported
public interface InventoryOperations {
    ProductDTO addStock(StockAdjustmentDTO dto);
    ProductDTO removeStock(StockAdjustmentDTO dto);
    List<ProductDTO> getLowStockProducts();
}
```

**2. Dependency Injection (`InventoryServiceImpl.java`)**

```java
@Service
public class InventoryServiceImpl implements InventoryService { // Implements Interface

    private final ProductRepository productRepository;

    // DEPENDENCY INJECTION (DIP):
    // We ask Spring to provide the repository, we don't say "new ProductRepository()"
    public InventoryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
```

---

## Exercise 5: Unit Testing with JUnit

**Requirement:** Service Layer (Mockito) and Controller Layer (MockMvc) testing.

### Implementation Logic

We verify that our code works in isolation (Unit Tests) and as a whole (Integration Tests).

### Code Evidence

**1. Service Test (`ProductServiceTest.java`)**
_Mocks the database so we verify logic without needing MySQL._

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository; // Fake Database

    @InjectMocks
    private ProductServiceImpl productService; // System Under Test

    @Test
    void create_ShouldReturnDto_WhenValid() {
        // Arrange
        when(productRepository.existsBySku(any())).thenReturn(false);
        when(productRepository.save(any())).thenReturn(product);

        // Act
        ProductDTO result = productService.create(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals("PROD-001", result.getSku());
    }
}
```

**2. Integration Test (`InventoryIntegrationTest.java`)**
_Tests the real flow with a test database._

```java
@SpringBootTest
@AutoConfigureMockMvc
class InventoryIntegrationTest {
    @Test
    void shouldManageInventoryFlow() throws Exception {
        // 1. Create Product
        mockMvc.perform(post("/api/products")...)
               .andExpect(status().isCreated());

        // 2. Add Stock
        mockMvc.perform(post("/api/inventory/stock-in")...)
               .andExpect(status().isOk());
    }
}
```

---

## Exercise 6: Advanced Functionality

**Requirement:** Security, Validation, Global Exception Handler.

### Implementation Logic

- **Security**: Used standard Spring Security Filter Chain strictly requiring authentication.
- **Exception Handling**: Centralized logic to catch errors and return JSON instead of crashes.

### Code Evidence

**1. Security Configuration (`SecurityConfig.java`)**

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // Public login
            .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin only
            .anyRequest().authenticated() // Everything else requires login
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**2. Global Exception Handler (`GlobalExceptionHandler.java`)**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

---

## Conclusion

The implemented system **fully complies** with the 2-week plan requirements. It includes a robust architectural foundation, security compliance, comprehensive testing coverage, and clean SOLID-principled code structure.
