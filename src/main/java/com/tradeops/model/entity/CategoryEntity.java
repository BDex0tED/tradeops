package com.tradeops.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    @Column(nullable = false)
    @ToString.Include
    private String name;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String slug;

    @Column(name = "is_active", nullable = false)
    @ToString.Include
    private boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<ProductEntity> products;
}