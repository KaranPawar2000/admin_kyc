package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.AdminDTO;
import com.Infinitio.kyc.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
