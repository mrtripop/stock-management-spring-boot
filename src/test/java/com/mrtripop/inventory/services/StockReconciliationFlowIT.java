package com.mrtripop.inventory.services;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.fixture.BrandFixture;
import com.mrtripop.clinical.fixture.MoleculeFixture;
import com.mrtripop.inventory.fixture.BatchFixture;
import com.mrtripop.inventory.fixture.ReconciliationStatusFixture;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;
import com.mrtripop.inventory.repository.BatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Stock Reconciliation Flow Integration Test")
class StockReconciliationFlowIT {

  @Autowired
  private StockReconciliationService stockReconciliationService;

  @Autowired
  private ReconciliationStatusService reconciliationStatusService;

  @Autowired
  private BatchRepository batchRepository;

  @Autowired
  private BrandRepository brandRepository;

  @Autowired
  private MoleculeRepository moleculeRepository;

  @Test
  @DisplayName("should progress from IN_PROGRESS to COMPLETED during full reconciliation")
  void shouldProgressFromInProgressToCompleted() {
    // Arrange
    seedBatches(5);

    // Act
    stockReconciliationService.reconcileAll();

    // Assert: Immediate status check (might be too fast, but @Async should give us a window)
    ReconciliationStatusDto initialStatus = reconciliationStatusService.getStatus();
    assertThat(initialStatus).isNotNull();
    assertThat(initialStatus.getStatus()).isEqualTo(ReconciliationStatusFixture.STATUS_IN_PROGRESS);
    assertThat(initialStatus.getProgress()).isGreaterThanOrEqualTo(0);

    // Assert: Poll for completion and verify progress increase
    AtomicInteger lastProgress = new AtomicInteger(-1);

    await()
        .atMost(Duration.ofSeconds(10))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(() -> {
          ReconciliationStatusDto currentStatus = reconciliationStatusService.getStatus();
          assertThat(currentStatus).isNotNull();

          if (ReconciliationStatusFixture.STATUS_IN_PROGRESS.equals(currentStatus.getStatus())) {
            assertThat(currentStatus.getProgress()).isGreaterThanOrEqualTo(lastProgress.get());
            lastProgress.set(currentStatus.getProgress());
          } else {
            assertThat(currentStatus.getStatus()).isEqualTo(ReconciliationStatusFixture.STATUS_COMPLETED);
          }
        });

    // Final verification
    ReconciliationStatusDto finalStatus = reconciliationStatusService.getStatus();
    assertThat(finalStatus.getStatus()).isEqualTo(ReconciliationStatusFixture.STATUS_COMPLETED);
    assertThat(finalStatus.getProgress()).isEqualTo(100);
  }

  private void seedBatches(int count) {
    Molecule molecule = moleculeRepository.save(MoleculeFixture.defaultEntity());
    Brand brand = brandRepository.save(BrandFixture.defaultEntity(molecule.getId()));

    for (int i = 0; i < count; i++) {
      Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusYears(1), brand);
      batch.setId(null);
      batch.setBatchNumber("BATCH-" + i);
      batchRepository.save(batch);
    }
  }
}
