package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.AdminDTO;
import com.Infinitio.kyc.dto.AdminDTOAdd;
import com.Infinitio.kyc.dto.AdminLoginRequest;
import com.Infinitio.kyc.dto.AdminLoginResponse;
import com.Infinitio.kyc.exception.OurException;
import com.Infinitio.kyc.service.AdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @GetMapping("/all")
    public ResponseEntity<List<AdminDTO>> getAllAdmins() {
        logger.info("Received request to fetch all admins");
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDTO> getAdminById(@PathVariable Integer id) {
        logger.info("Received request to fetch admin with id: {}", id);
        try {
            AdminDTO admin = adminService.getAdminById(id);
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            logger.error("Admin not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<AdminDTO> addAdmin(@RequestBody AdminDTOAdd adminDTOAdd) {
        logger.info("Received request to add a new admin");
        return ResponseEntity.ok(adminService.addAdmin(adminDTOAdd));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AdminDTO> updateAdmin(@PathVariable Integer id, @RequestBody AdminDTOAdd adminDTOAdd) {
        logger.info("Received request to update admin with id: {}", id);
        try {
            AdminDTO updatedAdmin = adminService.updateAdmin(id, adminDTOAdd);
            return ResponseEntity.ok(updatedAdmin);
        } catch (RuntimeException e) {
            logger.error("Failed to update admin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody @Valid AdminLoginRequest request) {
        logger.info("Received login request for email: {}", request.getEmailId());
        try {
            AdminLoginResponse response = adminService.login(request);
            logger.info("Login successful for email: {}", request.getEmailId());
            return ResponseEntity.ok(response);
        } catch (OurException e) {
            logger.error("Login failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage());
            throw new OurException("Internal server error");
        }
    }
}