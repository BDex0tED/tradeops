package com.tradeops.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mapstruct.ap.internal.conversion.BigDecimalToPrimitiveConversion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "order_number", unique = true)
    @ToString.Include
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private Trader trader;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ToString.Include
    @Column(name = "totals")
    private BigDecimal totals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_link_id")
    private CustomerLink customerLink;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines;

    @PrePersist
    public void onCreation(){
        this.createdAt = LocalDateTime.now();
    }

}
