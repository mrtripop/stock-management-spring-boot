package com.mrtripop.inventory.models.db;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "action_queue_tasks",
    indexes = {
        @Index(name = "aqt_store_status", columnList = "store_id, status"),
        @Index(name = "aqt_type_status", columnList = "task_type, status"),
        @Index(name = "aqt_batch_type_status", columnList = "batch_id, task_type, status")
    }
)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"store", "batch", "brand"})
public class Task extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "task_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "current_quantity")
    private Long currentQuantity;

    @Column(name = "threshold_quantity")
    private Long thresholdQuantity;

    @Column(name = "days_until_expiry")
    private Integer daysUntilExpiry;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}