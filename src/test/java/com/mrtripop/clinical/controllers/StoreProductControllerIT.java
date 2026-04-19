package com.mrtripop.clinical.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.dto.ActivateProductRequest;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StoreProductControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private StoreRepository storeRepository;
  @Autowired private MoleculeRepository moleculeRepository;
  @Autowired private BrandRepository brandRepository;
  @Autowired private StoreProductRepository storeProductRepository;
  @Autowired private AuditLedgerRepository auditLedgerRepository;

  private UUID storeId;
  private UUID moleculeId;
  private UUID brandId;

  @BeforeEach
  void setUp() {
    Store store = Store.builder().name("Test Pharmacy").type(com.mrtripop.clinical.models.db.StoreType.PHYSICAL).build();
    store = storeRepository.save(store);
    storeId = store.getId();

    Molecule molecule = Molecule.builder().genericName("Amoxicillin").therapeuticClass("Antibiotic").regulatorySchedule("Rx").build();
    molecule = moleculeRepository.save(molecule);
    moleculeId = molecule.getId();

    Brand brand = Brand.builder().molecule(molecule).brandName("Amoxil").strength("500mg").form("Capsule").baseUnit("strip").build();
    brand = brandRepository.save(brand);
    brandId = brand.getId();
  }

  @Nested
  @DisplayName("POST /activate")
  class ActivateProduct {

    @Test
    @DisplayName("should activate a brand for the store and return enriched response")
    void shouldActivateAndReturnEnrichedResponse() throws Exception {
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      String response =
          mockMvc
              .perform(
                  post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.code").value("201"))
              .andExpect(jsonPath("$.data.store_id").value(storeId.toString()))
              .andExpect(jsonPath("$.data.brand_id").value(brandId.toString()))
              .andExpect(jsonPath("$.data.is_active").value(true))
              .andExpect(jsonPath("$.data.brand_name").value("Amoxil"))
              .andExpect(jsonPath("$.data.molecule_generic_name").value("Amoxicillin"))
              .andExpect(jsonPath("$.data.therapeutic_class").value("Antibiotic"))
              .andReturn()
              .getResponse()
              .getContentAsString();

      String spId = objectMapper.readTree(response).path("data").path("id").asText();

      List<AuditLedger> audits = auditLedgerRepository.findByEntityIdOrderByTimestampDesc(spId.toString());
      assertThat(audits).anyMatch(a -> a.getActionType().equals("ACTIVATE_PRODUCT"));
    }

    @Test
    @DisplayName("should return 400 when brandId is missing")
    void whenBrandIdMissing_ShouldReturn400() throws Exception {
      String body = "{}";

      mockMvc
          .perform(
              post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 409 when brand already activated")
    void whenDuplicate_ShouldReturn409() throws Exception {
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      mockMvc
          .perform(
              post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      mockMvc
          .perform(
              post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 404 when store does not exist")
    void whenStoreNotFound_ShouldReturn404() throws Exception {
      UUID nonExistentStore = UUID.randomUUID();
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      mockMvc
          .perform(
              post("/api/v1/clinical/catalog/stores/" + nonExistentStore + "/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET / (list active products)")
  class ListActiveProducts {

    @Test
    @DisplayName("should return paginated active products enriched with catalog data")
    void shouldReturnPaginatedActiveProducts() throws Exception {
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      mockMvc
          .perform(
              post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      mockMvc
          .perform(get("/api/v1/clinical/catalog/stores/" + storeId + "/products")
              .param("page", "1")
              .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.content[0].brand_name").value("Amoxil"))
          .andExpect(jsonPath("$.data.content[0].molecule_generic_name").value("Amoxicillin"))
          .andExpect(jsonPath("$.data.content[0].strength").value("500mg"));
    }
  }

  @Nested
  @DisplayName("GET /{productId}")
  class GetStoreProduct {

    @Test
    @DisplayName("should return single enriched store product")
    void shouldReturnEnrichedProduct() throws Exception {
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      String response =
          mockMvc
              .perform(
                  post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andReturn()
              .getResponse()
              .getContentAsString();

      String spId = objectMapper.readTree(response).path("data").path("id").asText();

      mockMvc
          .perform(get("/api/v1/clinical/catalog/stores/" + storeId + "/products/" + spId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.brand_name").value("Amoxil"))
          .andExpect(jsonPath("$.data.form").value("Capsule"));
    }

    @Test
    @DisplayName("should return 404 when product not found")
    void whenNotFound_ShouldReturn404() throws Exception {
      mockMvc
          .perform(get("/api/v1/clinical/catalog/stores/" + storeId + "/products/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PATCH /{productId}")
  class UpdateOverride {

    @Test
    @DisplayName("should update price and shelf location")
    void shouldUpdateOverrides() throws Exception {
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      String response =
          mockMvc
              .perform(
                  post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andReturn()
              .getResponse()
              .getContentAsString();

      String spId = objectMapper.readTree(response).path("data").path("id").asText();

      UpdateOverrideRequest updateRequest =
          UpdateOverrideRequest.builder().price(new BigDecimal("14.99")).shelfLocation("C2").build();

      mockMvc
          .perform(
              patch("/api/v1/clinical/catalog/stores/" + storeId + "/products/" + spId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.price").value(14.99))
          .andExpect(jsonPath("$.data.shelf_location").value("C2"));

      List<AuditLedger> audits =
          auditLedgerRepository.findByEntityIdOrderByTimestampDesc(spId.toString());
      assertThat(audits).anyMatch(a -> a.getActionType().equals("UPDATE_OVERRIDE"));
    }
  }

  @Nested
  @DisplayName("DELETE /{productId}")
  class DeactivateProduct {

    @Test
    @DisplayName("should deactivate product (soft delete)")
    void shouldDeactivateProduct() throws Exception {
      ActivateProductRequest request = ActivateProductRequest.builder().brandId(brandId).build();

      String response =
          mockMvc
              .perform(
                  post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andReturn()
              .getResponse()
              .getContentAsString();

      String spId = objectMapper.readTree(response).path("data").path("id").asText();

      mockMvc
          .perform(delete("/api/v1/clinical/catalog/stores/" + storeId + "/products/" + spId))
          .andExpect(status().isNoContent());

      List<AuditLedger> audits =
          auditLedgerRepository.findByEntityIdOrderByTimestampDesc(spId.toString());
      assertThat(audits).anyMatch(a -> a.getActionType().equals("DEACTIVATE_PRODUCT"));

      // Verify deactivated product no longer appears in active list
      mockMvc
          .perform(get("/api/v1/clinical/catalog/stores/" + storeId + "/products")
              .param("page", "1").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isEmpty());
    }
  }

  @Nested
  @DisplayName("Full lifecycle")
  class FullLifecycle {

    @Test
    @DisplayName("should support full activate-update-deactivate lifecycle")
    void fullLifecycle() throws Exception {
      // 1. Activate
      ActivateProductRequest activateRequest = ActivateProductRequest.builder().brandId(brandId).build();
      String activateResponse =
          mockMvc
              .perform(
                  post("/api/v1/clinical/catalog/stores/" + storeId + "/products")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(activateRequest)))
              .andExpect(status().isCreated())
              .andReturn()
              .getResponse()
              .getContentAsString();

      UUID spId = UUID.fromString(objectMapper.readTree(activateResponse).path("data").path("id").asText());

      // 2. Verify in list
      mockMvc
          .perform(get("/api/v1/clinical/catalog/stores/" + storeId + "/products")
              .param("page", "1").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content.length()").value(1));

      // 3. Update override
      UpdateOverrideRequest updateRequest =
          UpdateOverrideRequest.builder().price(new BigDecimal("25.00")).shelfLocation("D1").build();
      mockMvc
          .perform(
              patch("/api/v1/clinical/catalog/stores/" + storeId + "/products/" + spId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.price").value(25.00));

      // 4. Deactivate
      mockMvc
          .perform(delete("/api/v1/clinical/catalog/stores/" + storeId + "/products/" + spId))
          .andExpect(status().isNoContent());

      // 5. Verify no longer in list
      mockMvc
          .perform(get("/api/v1/clinical/catalog/stores/" + storeId + "/products")
              .param("page", "1").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isEmpty());

      // 6. Verify audit trail has all 3 entries
      List<AuditLedger> audits = auditLedgerRepository.findByEntityIdOrderByTimestampDesc(spId.toString());
      assertThat(audits).hasSize(3);
      assertThat(audits.get(2).getActionType()).isEqualTo("ACTIVATE_PRODUCT");
      assertThat(audits.get(1).getActionType()).isEqualTo("UPDATE_OVERRIDE");
      assertThat(audits.get(0).getActionType()).isEqualTo("DEACTIVATE_PRODUCT");
    }
  }
}
