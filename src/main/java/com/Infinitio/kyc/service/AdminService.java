package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.AdminDTO;
import com.Infinitio.kyc.dto.AdminDTOAdd;
import com.Infinitio.kyc.dto.AdminLoginRequest;
import com.Infinitio.kyc.dto.AdminLoginResponse;
import com.Infinitio.kyc.entity.TbAdminMaster;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbRoleMaster;
import com.Infinitio.kyc.exception.OurException;
import com.Infinitio.kyc.repository.TbAdminMasterRepository;
import com.Infinitio.kyc.utils.DTOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private TbAdminMasterRepository adminRepository;

    @Autowired
    private DTOService dtoService;

    public List<AdminDTO> getAllAdmins() {
        try {
            logger.info("Fetching all admin users");
            List<TbAdminMaster> admins = adminRepository.findAll();

            if (admins.isEmpty()) {
                logger.info("No admin users found");
                return Collections.emptyList();
            }

            List<AdminDTO> adminDTOs = admins.stream()
                    .filter(Objects::nonNull)
                    .map(dtoService::convertAdminToDTO)
                    .collect(Collectors.toList());

            logger.info("Found {} admin users", adminDTOs.size());
            return adminDTOs;

        } catch (Exception e) {
            logger.error("Error while fetching admins: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch admin users", e);
        }
    }
    public AdminDTO getAdminById(Integer id) {
        try {
            logger.info("Fetching admin with id: {}", id);
            return adminRepository.findById(id)
                    .map(dtoService::convertAdminToDTO)
                    .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
        } catch (Exception e) {
            logger.error("Error while fetching admin with id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch admin", e);
        }
    }

    public AdminDTO addAdmin(AdminDTOAdd adminDTOAdd) {
        try {
            logger.info("Adding new admin for client ID: {}", adminDTOAdd.getClientId());
            TbAdminMaster admin = new TbAdminMaster();
            admin.setName(adminDTOAdd.getName());
            admin.setEmailId(adminDTOAdd.getEmailId());
            admin.setPassword(adminDTOAdd.getPassword()); // Consider encrypting this
            admin.setMobileNo(adminDTOAdd.getMobileNo());
            admin.setIsActive(adminDTOAdd.getIsActive());
            admin.setCreatedModifiedDate(java.time.LocalDateTime.now());
            admin.setReadOnly("N");
            admin.setArchiveFlag("F");

            // Set client
            TbClientMaster client = new TbClientMaster();
            client.setId(adminDTOAdd.getClientId());
            admin.setClient(client);
            admin.setClientId(1);

            // Set role
            TbRoleMaster role = new TbRoleMaster();
            role.setId(adminDTOAdd.getRoleId());
            admin.setRole(role);

            // Save to DB
            TbAdminMaster saved = adminRepository.save(admin);
            logger.info("Admin saved successfully with id {}", saved.getId());

            return dtoService.convertAdminToDTO(saved);
        } catch (Exception e) {
            logger.error("Failed to add admin: {}", e.getMessage());
            throw new RuntimeException("Failed to add admin", e);
        }
    }

    public AdminDTO updateAdmin(Integer id, AdminDTOAdd adminDTOAdd) {
        try {
            TbAdminMaster existingAdmin = adminRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));

            existingAdmin.setName(adminDTOAdd.getName());
            existingAdmin.setEmailId(adminDTOAdd.getEmailId());
            existingAdmin.setMobileNo(adminDTOAdd.getMobileNo());
            existingAdmin.setPassword(adminDTOAdd.getPassword()); // Consider encrypting
            existingAdmin.setIsActive(adminDTOAdd.getIsActive());
            existingAdmin.setCreatedModifiedDate(java.time.LocalDateTime.now());



            // Update role reference
            TbRoleMaster role = new TbRoleMaster();
            role.setId(adminDTOAdd.getRoleId());
            existingAdmin.setRole(role);

            TbAdminMaster updated = adminRepository.save(existingAdmin);
            return dtoService.convertAdminToDTO(updated);

        } catch (Exception e) {
            logger.error("Error updating admin with id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update admin", e);
        }
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        logger.info("Processing login request for email: {}", request.getEmailId());

        TbAdminMaster admin = adminRepository.findByEmailIdAndPassword(request.getEmailId(), request.getPassword())
                .orElseThrow(() -> {
                    logger.error("Login failed for email: {}", request.getEmailId());
                    return new OurException("Invalid credentials");
                });

        if (admin.getIsActive() == 0) {
            logger.error("Inactive admin account: {}", request.getEmailId());
            throw new OurException("Account is inactive");
        }

        AdminLoginResponse response = new AdminLoginResponse();
        response.setName(admin.getName());
        response.setRoleId(admin.getRole().getId());
        response.setUserId(admin.getClientId());

        logger.info("Login successful for email: {}", request.getEmailId());
        return response;
    }

}
