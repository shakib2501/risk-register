package com.hyperproof.riskregister.risk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

import java.time.Instant;

@Entity
public class Mitigation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2_000)
    private String description;

    @Column(nullable = false)
    private int effectiveness;

    @ManyToOne(optional = false)
    @JoinColumn(name = "risk_id", nullable = false)
    private Risk risk;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Mitigation() {
    }

    public Mitigation(String description, int effectiveness) {
        this.description = description;
        this.effectiveness = effectiveness;
    }

    void setRisk(Risk risk) {
        this.risk = risk;
    }

    public void update(String description, int effectiveness) {
        this.description = description;
        this.effectiveness = effectiveness;
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public Risk getRisk() {
        return risk;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getEffectiveness() {
        return effectiveness;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
