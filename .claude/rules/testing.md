# Testing Standards

## Test Naming

Always use `@DisplayName` with a descriptive name that tells the business rule or scenario being tested. Do not rely solely on method names — use business wording.

```java
@DisplayName("should throw NotFoundException when molecule ID does not exist")
@Test void getMolecule_WithNonExistentId_ShouldThrowNotFoundException() { ... }
```

## Test Organization

Use `@Nested` classes to group related test scenarios. Each nested class represents a test scene (e.g., a specific method or business rule). Use another level of `@Nested` for sub-scenarios when needed.

```java
class MasterCatalogServiceImplTest {

  @Nested
  @DisplayName("Create molecule ")
  class CreateMolecule {

    @Test
    @DisplayName("should save and return molecule DTO")
    void shouldSaveAndReturnDto() { ... }

    @Test
    @DisplayName("should throw DuplicateMoleculeException when generic name already exists")
    void whenGenericNameExists_ShouldThrowDuplicate() { ... }
  }

  @Nested
  @DisplayName("createBrand")
  class CreateBrand { ... }
}
```

## AAA Pattern

All test methods must follow Arrange-Act-Assert structure. Do not mix phases. Add a blank line between each phase.

```java
@Test
void shouldSaveAndReturnDto() {
  // Arrange
  MoleculeDto input = MoleculeFixture.validDto();
  Molecule saved = MoleculeFixture.defaultEntity();
  when(moleculeRepository.save(any())).thenReturn(saved);

  // Act
  MoleculeDto result = masterCatalogService.createMolecule(input);

  // Assert
  assertNotNull(result.getId());
  assertEquals("Paracetamol", result.getGenericName());
  verify(moleculeRepository).save(any());
}
```

## Test Data Creation (Fixture Pattern)

Use Object Mother or Fixture patterns to create test data. Do not build DTOs/entities inline in test methods. Centralize test data creation in a fixture class per domain.

```java
// src/test/java/com/mrtripop/clinical/fixture/MoleculeFixture.java
public final class MoleculeFixture {
  private MoleculeFixture() {}

  public static MoleculeDto validDto() {
    return MoleculeDto.builder().genericName("Paracetamol").build();
  }

  public static Molecule defaultEntity() {
    return Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();
  }
}
```

## Unit Test Setup

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceName")
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
