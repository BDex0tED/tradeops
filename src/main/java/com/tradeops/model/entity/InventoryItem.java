package com.tradeops.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "qty_on_hand")
    @ToString.Include
    private Integer qtyOnHand = 0;

    @Column(name = "qty_reserved")
    @ToString.Include
    private Integer qtyReserved = 0;

    @Column(name = "warehouse_id")
    @ToString.Include
    private Long warehouseId;

    @Column(name = "reorder_level")
    @ToString.Include
    private Integer reorderLevel = 0;

    @Version
    private Long version;
}
