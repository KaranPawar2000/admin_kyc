package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.AdminDTO;
import com.Infinitio.kyc.entity.TbAdminMaster;
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

}
