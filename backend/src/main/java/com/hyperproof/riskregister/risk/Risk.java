package com.hyperproof.riskregister.risk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@Entity
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2_000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskCategory category;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private int likelihood;

    @Column(nullable = false)
    private int impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskStatus status = RiskStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private int mitigationCount;

    protected Risk() {
    }

    public Risk(String title, String description, RiskCategory category, String owner, int likelihood, int impact) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.owner = owner;
        this.likelihood = likelihood;
        this.impact = impact;
    }

    public void addMitigation() {
        mitigationCount++;
    }

    public void changeStatus(RiskStatus newStatus) {
        if (newStatus == RiskStatus.CLOSED && mitigationCount == 0) {
            throw new IllegalStateException("A risk cannot be closed without at least one mitigation");
        }

        status = newStatus;
    }

    public RiskStatus status() {
        return status;
    }

    @PrePersist
    void setCreatedAt() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void setUpdatedAt() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
