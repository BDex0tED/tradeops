package com.tradeops.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString( onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer_links")
public class CustomerLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "customer_external_id")
    @ToString.Include
    private Long customerExternalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id")
    private Trader trader;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "contact_hash")
    private String contactHash;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

}
