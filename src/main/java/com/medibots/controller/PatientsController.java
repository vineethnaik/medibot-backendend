package com.medibots.controller;

import com.medibots.entity.Patient;
import com.medibots.entity.Profile;
import com.medibots.repository.PatientRepository;
import com.medibots.repository.ProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientsController {
    private final PatientRepository patientRepo;
    private final ProfileRepository profileRepo;

    public PatientsController(PatientRepository patientRepo, ProfileRepository profileRepo) {
        this.patientRepo = patientRepo;
        this.profileRepo = profileRepo;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> list() {
        return ResponseEntity.ok(patientRepo.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/me")
    public ResponseEntity<Patient> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return patientRepo.findByUserId(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok().build());
    }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        String userId = auth.getName();
        Patient p = new Patient();
        p.setUserId(userId);
        p.setFullName((String) body.get("full_name"));
        if (body.get("dob") != null) p.setDob(LocalDate.parse(body.get("dob").toString()));
        p.setGender((String) body.get("gender"));
        p.setInsuranceProvider((String) body.get("insurance_provider"));
        p.setPolicyNumber((String) body.get("policy_number"));
        if (body.get("hospital_id") != null) p.setHospitalId((String) body.get("hospital_id"));
        p = patientRepo.save(p);
        if (body.get("hospital_id") != null) {
            profileRepo.findByUserId(userId).ifPresent(pr -> {
                pr.setHospitalId((String) body.get("hospital_id"));
                profileRepo.save(pr);
            });
        }
        return ResponseEntity.ok(p);
    }
}
