package com.medibots.controller;

import com.medibots.entity.Appointment;
import com.medibots.entity.Patient;
import com.medibots.repository.AppointmentRepository;
import com.medibots.repository.PatientRepository;
import com.medibots.repository.ProfileRepository;
import com.medibots.repository.UserRoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {
    private final AppointmentRepository appointmentRepo;
    private final UserRoleRepository userRoleRepo;
    private final PatientRepository patientRepo;
    private final ProfileRepository profileRepo;

    public AppointmentsController(AppointmentRepository appointmentRepo, UserRoleRepository userRoleRepo, PatientRepository patientRepo, ProfileRepository profileRepo) {
        this.appointmentRepo = appointmentRepo;
        this.userRoleRepo = userRoleRepo;
        this.patientRepo = patientRepo;
        this.profileRepo = profileRepo;
    }

    @GetMapping("/doctor")
    public ResponseEntity<List<Map<String, Object>>> doctorList(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        List<Appointment> list = appointmentRepo.findByDoctorIdOrderByAppointmentDateDesc(auth.getName());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Appointment a : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("patient_id", a.getPatientId());
            m.put("doctor_id", a.getDoctorId());
            m.put("status", a.getStatus());
            m.put("appointment_date", a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : null);
            m.put("reason", a.getReason());
            m.put("consultation_fee", a.getConsultationFee());
            m.put("fee_paid", a.getFeePaid());
            m.put("hospital_id", a.getHospitalId());
            m.put("created_at", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
            Map<String, Object> patientMap = new HashMap<>();
            String displayName = "Patient";
            var patientOpt = patientRepo.findById(a.getPatientId());
            if (patientOpt.isPresent()) {
                Patient p = patientOpt.get();
                if (p.getFullName() != null && !p.getFullName().isBlank()) {
                    displayName = p.getFullName().trim();
                } else {
                    displayName = profileRepo.findByUserId(p.getUserId())
                            .map(pr -> (pr.getName() != null && !pr.getName().isBlank()) ? pr.getName().trim() : "Patient")
                            .orElse("Patient");
                }
            }
            patientMap.put("full_name", displayName);
            patientMap.put("fullName", displayName);
            m.put("patients", patientMap);
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/patient")
    public ResponseEntity<List<Appointment>> patientList(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        var patient = patientRepo.findByUserId(auth.getName());
        if (patient.isEmpty()) return ResponseEntity.ok(List.of());
        List<Appointment> list = appointmentRepo.findByPatientIdOrderByAppointmentDateDesc(patient.get().getId());
        return ResponseEntity.ok(list);
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> list(Authentication auth) {
        String uid = auth != null ? auth.getName() : null;
        if (uid != null && userRoleRepo.findByUserId(uid).map(ur -> "DOCTOR".equals(ur.getRole())).orElse(false))
            return doctorList(auth);
        return ResponseEntity.ok(appointmentRepo.findAllByOrderByAppointmentDateDesc());
    }

    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Appointment a = new Appointment();
        a.setPatientId((String) body.get("patient_id"));
        a.setDoctorId((String) body.get("doctor_id"));
        a.setStatus("PENDING");
        if (body.get("appointment_date") != null)
            a.setAppointmentDate(Instant.parse(body.get("appointment_date").toString()));
        a.setReason((String) body.get("reason"));
        if (body.get("hospital_id") != null) a.setHospitalId((String) body.get("hospital_id"));
        if (body.get("consultation_fee") != null) a.setConsultationFee(new java.math.BigDecimal(body.get("consultation_fee").toString()));
        if (body.get("fee_paid") != null) a.setFeePaid(Boolean.TRUE.equals(body.get("fee_paid")));
        a = appointmentRepo.save(a);
        return ResponseEntity.ok(a);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Appointment> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Appointment a = appointmentRepo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (body.get("status") != null) a.setStatus(body.get("status"));
        a = appointmentRepo.save(a);
        return ResponseEntity.ok(a);
    }
}
