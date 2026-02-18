package com.medibots.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;
    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;
    @Column(nullable = false, length = 32)
    private String status = "PENDING";
    @Column(name = "appointment_date", nullable = false)
    private Instant appointmentDate;
    private String reason;
    private String notes;
    @Column(name = "consultation_fee", precision = 12, scale = 2)
    private BigDecimal consultationFee;
    @Column(name = "fee_paid")
    private Boolean feePaid;
    @Column(name = "hospital_id", length = 36)
    private String hospitalId;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = java.util.UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Instant appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public Boolean getFeePaid() { return feePaid; }
    public void setFeePaid(Boolean feePaid) { this.feePaid = feePaid; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
