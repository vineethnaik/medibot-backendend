package com.medibots.controller;

import com.medibots.entity.Claim;
import com.medibots.entity.Patient;
import com.medibots.repository.ClaimRepository;
import com.medibots.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/claims")
public class ClaimsController {
    private final ClaimRepository claimRepo;
    private final PatientRepository patientRepo;

    public ClaimsController(ClaimRepository claimRepo, PatientRepository patientRepo) {
        this.claimRepo = claimRepo;
        this.patientRepo = patientRepo;
    }

    private String userId(Authentication auth) {
        return auth != null ? auth.getName() : null;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(Authentication auth) {
        List<Claim> claims = claimRepo.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> out = claims.stream().map(c -> toMap(c)).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/patient")
    public ResponseEntity<List<Map<String, Object>>> patientClaims(Authentication auth) {
        String uid = userId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        Patient p = patientRepo.findByUserId(uid).orElse(null);
        if (p == null) return ResponseEntity.ok(List.of());
        List<Claim> claims = claimRepo.findByPatientIdOrderByCreatedAtDesc(p.getId());
        return ResponseEntity.ok(claims.stream().map(this::toMap).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, Authentication auth) {
        String uid = userId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        Claim c = new Claim();
        c.setPatientId((String) body.get("patient_id"));
        c.setInsuranceProvider((String) body.get("insurance_provider"));
        c.setAmount(body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : BigDecimal.ZERO);
        c.setSubmittedBy(uid);
        if (body.get("appointment_id") != null) c.setAppointmentId((String) body.get("appointment_id"));
        c = claimRepo.save(c);
        return ResponseEntity.ok(toMap(c));
    }

    @PostMapping("/manage")
    public ResponseEntity<Map<String, Object>> manage(@RequestBody Map<String, String> body, Authentication auth) {
        String claimId = body.get("claim_id");
        String action = body.get("action");
        Claim c = claimRepo.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found"));
        if ("approve".equals(action)) c.setStatus("APPROVED");
        else if ("reject".equals(action)) c.setStatus("DENIED");
        c.setProcessedAt(Instant.now());
        c = claimRepo.save(c);
        return ResponseEntity.ok(toMap(c));
    }

    private Map<String, Object> toMap(Claim c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("claim_number", c.getClaimNumber());
        m.put("patient_id", c.getPatientId());
        m.put("insurance_provider", c.getInsuranceProvider());
        m.put("amount", c.getAmount());
        m.put("status", c.getStatus());
        m.put("ai_risk_score", c.getAiRiskScore());
        m.put("ai_explanation", c.getAiExplanation());
        m.put("submitted_by", c.getSubmittedBy());
        m.put("submitted_at", c.getSubmittedAt());
        m.put("processed_at", c.getProcessedAt());
        m.put("appointment_id", c.getAppointmentId());
        m.put("created_at", c.getCreatedAt());
        patientRepo.findById(c.getPatientId()).ifPresent(p -> {
            m.put("patients", Map.of("full_name", p.getFullName() != null ? p.getFullName() : "", "user_id", p.getUserId() != null ? p.getUserId() : ""));
        });
        return m;
    }
}
