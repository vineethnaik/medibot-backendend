package com.medibots.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "claim_number", unique = true, nullable = false)
    private String claimNumber;
    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;
    @Column(name = "insurance_provider", nullable = false)
    private String insuranceProvider;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
    @Column(nullable = false, length = 32)
    private String status = "PENDING";
    @Column(name = "ai_risk_score", precision = 5, scale = 2)
    private BigDecimal aiRiskScore;
    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;
    @Column(name = "submitted_by", nullable = false, length = 36)
    private String submittedBy;
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
    @Column(name = "processed_at")
    private Instant processedAt;
    @Column(name = "appointment_id", length = 36)
    private String appointmentId;
    @Column(name = "hospital_id", length = 36)
    private String hospitalId;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = java.util.UUID.randomUUID().toString();
        if (claimNumber == null) claimNumber = "CLM-" + id.substring(0, 8);
        if (submittedAt == null) submittedAt = Instant.now();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAiRiskScore() { return aiRiskScore; }
    public void setAiRiskScore(BigDecimal aiRiskScore) { this.aiRiskScore = aiRiskScore; }
    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
