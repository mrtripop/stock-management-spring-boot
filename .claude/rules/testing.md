---
paths:
  - "src/test/**/*.java"
---

# Testing Standards

## Test Naming

Always use `@DisplayName` with a descriptive name that tells the business rule or scenario being tested. Do not rely solely on method names — use business wording.

```java
@DisplayName("Should response not found when molecule ID does not exist")
@Test void getMolecule_WithNonExistentId_ShouldThrowNotFoundException() { ... }
```

## Test Organization

Use `@Nested` classes to group related test scenarios. Each nested class represents a business rule (or test scene). Use another level of `@Nested` for sub-scenarios when needed.

```java
class MasterCatalogServiceImplTest {

  @Nested
  @DisplayName("User create new product molecule")
  class CreateMolecule {

    @Test
    @DisplayName("should save new molecule when generic name doesn't exists")
    void shouldSaveNewMolecule_whenGenericNameDoesNotExists() { ... }

    @Test
    @DisplayName("should skip duplicate molecule and tell user when generic name already exists")
    void shouldThrowDuplicate_whenGenericNameExists() { ... }
  }

  @Nested
  @DisplayName("User create new brand")
  class CreateBrand { ... }
}
```

## AAA Pattern

All test methods must follow `Arrange-Act-Assert` structure. Do not mix phases. Add a blank line between each phase.

```java
@Test
@DisplayName("should save new molecule when generic name doesn't exists")
void shouldSaveAndReturnDto() {
  // Arrange
  MoleculeDto input = MoleculeFixture.validDto();
  Molecule saved = MoleculeFixture.defaultEntity();
  when(moleculeRepository.save(any())).thenReturn(saved);

  // Act
  MoleculeDto result = masterCatalogService.createMolecule(input);

  // Assert
  assertNotNull(result.getId());
  assertEquals(MoleculeFixture.VALID_GENERIC_NAME, result.getGenericName());
  verify(moleculeRepository).save(any());
}
```

## Test Data Creation (Fixture Pattern)

Use `Object Mother` or `Fixture patterns` to create test data. Do not build DTOs/entities inline in test methods. Centralize test data creation in a fixture class per domain.

Define shared expected values as constants in the fixture. Factory methods and assertions must reference the same constants — never hardcode literals in both places.

```java
// src/test/java/com/mrtripop/clinical/fixture/MoleculeFixture.java
public final class MoleculeFixture {
  private MoleculeFixture() {}

  // Shared constants — single source of truth for both setup and assertions
  public static final String VALID_GENERIC_NAME = "Paracetamol";
  public static final String VALID_THERAPEUTIC_CLASS = "Analgesic";

  public static MoleculeDto validDto() {
    return MoleculeDto.builder()
        .genericName(VALID_GENERIC_NAME)
        .therapeuticClass(VALID_THERAPEUTIC_CLASS)
        .build();
  }

  public static Molecule defaultEntity() {
    return Molecule.builder()
        .id(UUID.randomUUID())
        .genericName(VALID_GENERIC_NAME)
        .therapeuticClass(VALID_THERAPEUTIC_CLASS)
        .build();
  }
}
```

**Assertion rules — use constants, not hardcoded literals:**

```java
// Wrong — hardcoded string duplicated from fixture
assertEquals("Paracetamol", result.getGenericName());

// Right — assert against fixture constant
assertEquals(MoleculeFixture.VALID_GENERIC_NAME, result.getGenericName());

// Also right — when output should match the input, assert against the input itself
assertEquals(input.getGenericName(), result.getGenericName());
```

| Scenario | Approach |
|----------|----------|
| Output should match what was inputted | Assert against input (`input.getX()`) |
| Multiple tests need the same expected value | Fixture constant (`Fixture.VALID_NAME`) |
| Asserting enum, status, or default behavior | Direct value or enum reference |

## Unit Test Setup

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Description of the service in business wording")
class ServiceNameTest {

  @Mock private SomeRepository someRepository;
  @InjectMocks private ServiceImpl service;

  @BeforeEach
  void setUp() {
    // Stub default save behavior (echo entity back, simulating JPA)
    when(someRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
  }
}
```

## Integration Test Setup (SQL Query Tests Only)

API/HTTP integration tests (MockMvc, WebTestClient) are **not** used. Integration tests are limited to **SQL query tests** that verify repository queries, entity mappings, and database constraints.

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SomeRepositoryTest {

  @Autowired private SomeRepository someRepository;

  @Test
  @DisplayName("should find active products by store id with entity graph")
  void shouldFindActiveProductsByStoreId() {
    // Arrange: seed data via repository.save()
    // Act: call repository query method
    // Assert: verify returned entities and fetched associations
  }
}
```

- Integration tests use `IT` suffix (e.g., `StoreProductRepositoryIT`)
- H2 in-memory with PostgreSQL compatibility mode (`spring.datasource.url=jdbc:h2:...;MODE=PostgreSQL`)
- `create-drop` DDL strategy for test isolation
- Cache type set to `none` in `application-test.yml`
- Test repository queries, entity relationships (`@EntityGraph`, `JOIN FETCH`), constraints, and pagination
- **Do not** use `@AutoConfigureMockMvc`, `MockMvc`, or `WebTestClient` in integration tests
