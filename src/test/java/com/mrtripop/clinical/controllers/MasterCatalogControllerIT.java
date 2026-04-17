package com.mrtripop.clinical.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MasterCatalogControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private AuditLedgerRepository auditLedgerRepository;

  @Test
  void shouldCreateAndRetrieveMolecule() throws Exception {
    MoleculeDto moleculeDto =
        MoleculeDto.builder()
            .genericName("Paracetamol")
            .therapeuticClass("Analgesic")
            .regulatorySchedule("Over-the-counter")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/clinical/catalog/molecules")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(moleculeDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("201"))
            .andExpect(jsonPath("$.data.generic_name").value("Paracetamol"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID moleculeId = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());

    mockMvc
        .perform(get("/api/v1/clinical/catalog/molecules/" + moleculeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.generic_name").value("Paracetamol"));
  }

  @Test
  void shouldCreateAndRetrieveBrand() throws Exception {
    MoleculeDto moleculeDto =
        MoleculeDto.builder()
            .genericName("Amoxicillin")
            .therapeuticClass("Antibiotic")
            .build();

    String molResponse =
        mockMvc
            .perform(
                post("/api/v1/clinical/catalog/molecules")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(moleculeDto)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID moleculeId = UUID.fromString(objectMapper.readTree(molResponse).path("data").path("id").asText());

    BrandDto brandDto =
        BrandDto.builder()
            .moleculeId(moleculeId)
            .brandName("Amoxil")
            .strength("500mg")
            .form("Capsule")
            .build();

    mockMvc
        .perform(
            post("/api/v1/clinical/catalog/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(brandDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.brand_name").value("Amoxil"));

    mockMvc
        .perform(get("/api/v1/clinical/catalog/molecules/" + moleculeId + "/brands"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].brand_name").value("Amoxil"));
  }

  @Test
  void shouldReturn400WhenMoleculeGenericNameIsMissing() throws Exception {
    MoleculeDto moleculeDto = MoleculeDto.builder().build();

    mockMvc
        .perform(
            post("/api/v1/clinical/catalog/molecules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moleculeDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldUpdateMoleculeMetadataAndLogAudit() throws Exception {
    // 1. Create Molecule
    MoleculeDto moleculeDto =
        MoleculeDto.builder()
            .genericName("Ibuprofen")
            .therapeuticClass("NSAID")
            .regulatorySchedule("OTC")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/clinical/catalog/molecules")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(moleculeDto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID moleculeId = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());

    // 2. Update Metadata
    MoleculeDto updateDto =
        MoleculeDto.builder()
            .therapeuticClass("Analgesic")
            .regulatorySchedule("Rx")
            .build();

    mockMvc
        .perform(
            patch("/api/v1/clinical/catalog/molecules/" + moleculeId + "/metadata")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.therapeutic_class").value("Analgesic"))
        .andExpect(jsonPath("$.data.regulatory_schedule").value("Rx"));

    // 3. Verify Audit Ledger
    List<AuditLedger> auditLogs = auditLedgerRepository.findAll();
    assertThat(auditLogs).isNotEmpty();
    AuditLedger lastAudit = auditLogs.get(auditLogs.size() - 1);
    assertThat(lastAudit.getActionType()).isEqualTo("UPDATE_METADATA");
    assertThat(lastAudit.getEntityId()).isEqualTo(moleculeId.toString());
    assertThat(lastAudit.getNewValue()).contains("therapeuticClass=Analgesic");
    assertThat(lastAudit.getNewValue()).contains("regulatorySchedule=Rx");
  }

  @Test
  void shouldSearchMolecules() throws Exception {
    MoleculeDto moleculeDto =
        MoleculeDto.builder()
            .genericName("Cetirizine")
            .therapeuticClass("Antihistamine")
            .build();

    mockMvc
        .perform(
            post("/api/v1/clinical/catalog/molecules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moleculeDto)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/clinical/catalog/molecules/search").param("query", "ceti"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].generic_name").value("Cetirizine"))
        .andExpect(jsonPath("$.data[0].therapeutic_class").value("Antihistamine"));
  }
}
