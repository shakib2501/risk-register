package com.hyperproof.riskregister.risk;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private LocalDate nextReviewDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskStatus status = RiskStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "risk", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mitigation> mitigations = new ArrayList<>();

    protected Risk() {
    }

    public Risk(String title, String description, RiskCategory category, String owner, int likelihood, int impact) {
        this(title, description, category, owner, likelihood, impact, null);
    }

    public Risk(
            String title,
            String description,
            RiskCategory category,
            String owner,
            int likelihood,
            int impact,
            LocalDate nextReviewDate
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.owner = owner;
        this.likelihood = likelihood;
        this.impact = impact;
        this.nextReviewDate = nextReviewDate;
    }

    public void addMitigation(Mitigation mitigation) {
        Mitigation nonNullMitigation = Objects.requireNonNull(mitigation, "Mitigation is required");
        nonNullMitigation.setRisk(this);
        mitigations.add(nonNullMitigation);
        if (status == RiskStatus.OPEN) {
            status = RiskStatus.MITIGATING;
        }
    }

    public void removeMitigation(Mitigation mitigation) {
        mitigations.remove(mitigation);
    }

    public void changeStatus(RiskStatus newStatus) {
        if (newStatus == RiskStatus.CLOSED && mitigations.isEmpty()) {
            throw new IllegalStateException("A risk cannot be closed without at least one mitigation");
        }
        if (newStatus == RiskStatus.OPEN && !mitigations.isEmpty()) {
            throw new IllegalStateException("A risk with mitigations cannot be open");
        }

        status = newStatus;
    }

    public void update(
            String title,
            String description,
            RiskCategory category,
            String owner,
            int likelihood,
            int impact,
            RiskStatus newStatus,
            LocalDate nextReviewDate
    ) {
        if (newStatus != null) {
            changeStatus(newStatus);
        }
        this.title = title;
        this.description = description;
        this.category = category;
        this.owner = owner;
        this.likelihood = likelihood;
        this.impact = impact;
        this.nextReviewDate = nextReviewDate;
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

    public String getDescription() {
        return description;
    }

    public RiskCategory getCategory() {
        return category;
    }

    public String getOwner() {
        return owner;
    }

    public int getLikelihood() {
        return likelihood;
    }

    public int getImpact() {
        return impact;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public boolean isReviewOverdue(LocalDate referenceDate) {
        return nextReviewDate != null && nextReviewDate.isBefore(referenceDate);
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

    public int getMitigationCount() {
        return mitigations.size();
    }

    public List<Mitigation> getMitigations() {
        return List.copyOf(mitigations);
    }
}
