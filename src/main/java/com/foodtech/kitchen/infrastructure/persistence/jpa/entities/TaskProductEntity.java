package com.foodtech.kitchen.infrastructure.persistence.jpa.entities;

import com.foodtech.kitchen.domain.model.ProductType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    @Column
    private Integer preparationTimeSeconds;

    // JPA maneja esta relación automáticamente con @JoinColumn en TaskEntity
    @Column(name = "task_id", insertable = false, updatable = false)
    private Long taskId;
}