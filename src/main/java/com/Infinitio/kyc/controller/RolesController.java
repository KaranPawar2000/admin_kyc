package com.Infinitio.kyc.controller;

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

}
