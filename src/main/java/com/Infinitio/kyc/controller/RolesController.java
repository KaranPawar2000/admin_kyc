package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.BulkRoleFormsUpdateRequest;
import com.Infinitio.kyc.dto.RoleDTO;
import com.Infinitio.kyc.dto.RoleFormDTO;
import com.Infinitio.kyc.service.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolesController {

    @Autowired
    private RolesService rolesService;


    private static final Logger logger = LoggerFactory.getLogger(RolesController.class);

    @Autowired
    private RolesService roleService;

    @GetMapping("/all")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        logger.info("Received request to fetch all roles");
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{roleId}/forms")
    public ResponseEntity<List<RoleFormDTO>> getFormsByRole(@PathVariable Integer roleId) {
        logger.info("Received request to fetch forms for role ID {}", roleId);
        List<RoleFormDTO> formList = roleService.getFormsByRoleId(roleId);
        return ResponseEntity.ok(formList);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRole(@RequestBody RoleDTO roleDTO) {
        logger.info("Received request to add new role: {}", roleDTO.getName());
        try {
            RoleDTO savedRoleDTO = rolesService.addRole(roleDTO);  // return saved RoleDTO
            return ResponseEntity.ok(savedRoleDTO);
        } catch (Exception e) {
            logger.error("Error adding role: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to add role: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Integer id, @RequestBody RoleDTO roleDTO) {
        logger.info("Received request to update role ID: {}", id);
        try {
            RoleDTO updatedRole = rolesService.updateRole(id, roleDTO);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            logger.error("Error updating role: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to update role: " + e.getMessage());
        }
    }

    @PatchMapping("/forms/bulk-update")
    public ResponseEntity<?> updateMultipleFormsForRole(@RequestBody BulkRoleFormsUpdateRequest request) {
        logger.info("Received request to update multiple forms for role ID: {}", request.getRoleId());
        try {
            List<RoleFormDTO> updatedForms = rolesService.updateMultipleFormsForRole(request);
            return ResponseEntity.ok(updatedForms);
        } catch (Exception e) {
            logger.error("Error updating forms for role ID {}: {}", request.getRoleId(), e.getMessage());
            return ResponseEntity.status(500).body("Failed to update forms: " + e.getMessage());
        }
    }


}
