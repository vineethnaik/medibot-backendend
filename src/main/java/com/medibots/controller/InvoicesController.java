package com.medibots.controller;

import com.medibots.entity.Claim;
import com.medibots.entity.Invoice;
import com.medibots.entity.InvoiceItem;
import com.medibots.entity.Patient;
import com.medibots.repository.ClaimRepository;
import com.medibots.repository.InvoiceRepository;
import com.medibots.repository.InvoiceItemRepository;
import com.medibots.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoicesController {
    private final InvoiceRepository invoiceRepo;
    private final InvoiceItemRepository itemRepo;
    private final PatientRepository patientRepo;
    private final ClaimRepository claimRepo;

    public InvoicesController(InvoiceRepository invoiceRepo, InvoiceItemRepository itemRepo, PatientRepository patientRepo, ClaimRepository claimRepo) {
        this.invoiceRepo = invoiceRepo;
        this.itemRepo = itemRepo;
        this.patientRepo = patientRepo;
        this.claimRepo = claimRepo;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Invoice> all = invoiceRepo.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Invoice i : all) {
            Map<String, Object> m = toMap(i);
            patientRepo.findById(i.getPatientId()).ifPresent(p -> m.put("patients", Map.of("full_name", p.getFullName(), "user_id", p.getUserId())));
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/patient")
    public ResponseEntity<List<Map<String, Object>>> patientInvoices(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Patient p = patientRepo.findByUserId(auth.getName()).orElse(null);
        if (p == null) return ResponseEntity.ok(List.of());
        List<Invoice> list = invoiceRepo.findByPatientIdOrderByCreatedAtDesc(p.getId());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Invoice i : list) {
            Map<String, Object> m = toMap(i);
            m.put("patients", Map.of("full_name", p.getFullName(), "user_id", p.getUserId()));
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<InvoiceItem>> items(@PathVariable String id) {
        return ResponseEntity.ok(itemRepo.findByInvoiceIdOrderByCreatedAtAsc(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        String patientId = (String) body.get("patient_id");
        if (patientId == null) throw new RuntimeException("patient_id required");
        Invoice inv = new Invoice();
        inv.setPatientId(patientId);
        inv.setTotalAmount(body.get("total_amount") != null ? new BigDecimal(body.get("total_amount").toString()) : BigDecimal.ZERO);
        if (body.get("hospital_id") != null) inv.setHospitalId((String) body.get("hospital_id"));
        inv.setPaymentStatus(body.get("payment_status") != null ? (String) body.get("payment_status") : "UNPAID");
        if (inv.getDueDate() == null) inv.setDueDate(LocalDate.now().plusDays(30));
        inv = invoiceRepo.save(inv);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineItems = (List<Map<String, Object>>) body.get("line_items");
        if (lineItems != null) {
            for (Map<String, Object> li : lineItems) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoiceId(inv.getId());
                item.setDescription((String) li.get("description"));
                item.setAmount(li.get("amount") != null ? new BigDecimal(li.get("amount").toString()) : BigDecimal.ZERO);
                item.setItemType((String) li.getOrDefault("item_type", "CONSULTATION"));
                itemRepo.save(item);
            }
        }
        Map<String, Object> m = toMap(inv);
        patientRepo.findById(inv.getPatientId()).ifPresent(p -> m.put("patients", Map.of("full_name", p.getFullName(), "user_id", p.getUserId())));
        return ResponseEntity.ok(m);
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        String claimId = (String) body.get("claim_id");
        String patientId = (String) body.get("patient_id");
        if (patientId == null && claimId != null) {
            patientId = claimRepo.findById(claimId).map(Claim::getPatientId).orElse(null);
        }
        if (patientId == null)
            throw new RuntimeException("patient_id or claim_id required");
        Invoice inv = new Invoice();
        inv.setClaimId(claimId);
        inv.setPatientId(patientId);
        inv.setTotalAmount(body.get("total_amount") != null ? new BigDecimal(body.get("total_amount").toString()) : BigDecimal.ZERO);
        inv.setDueDate(LocalDate.now().plusDays(30));
        inv = invoiceRepo.save(inv);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineItems = (List<Map<String, Object>>) body.get("line_items");
        if (lineItems != null) {
            for (Map<String, Object> li : lineItems) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoiceId(inv.getId());
                item.setDescription((String) li.get("description"));
                item.setAmount(li.get("amount") != null ? new BigDecimal(li.get("amount").toString()) : BigDecimal.ZERO);
                item.setItemType((String) li.getOrDefault("item_type", "CONSULTATION"));
                itemRepo.save(item);
            }
        }
        Map<String, Object> m = toMap(inv);
        patientRepo.findById(inv.getPatientId()).ifPresent(p -> m.put("patients", Map.of("full_name", p.getFullName(), "user_id", p.getUserId())));
        return ResponseEntity.ok(m);
    }

    private Map<String, Object> toMap(Invoice i) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", i.getId());
        m.put("invoice_number", i.getInvoiceNumber());
        m.put("patient_id", i.getPatientId());
        m.put("claim_id", i.getClaimId());
        m.put("total_amount", i.getTotalAmount());
        m.put("due_date", i.getDueDate());
        m.put("payment_status", i.getPaymentStatus());
        m.put("created_at", i.getCreatedAt());
        return m;
    }
}
