package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.RoleDTO;
import com.Infinitio.kyc.dto.RoleFormDTO;
import com.Infinitio.kyc.entity.TbRoleDetails;
import com.Infinitio.kyc.entity.TbRoleMaster;
import com.Infinitio.kyc.repository.TbRoleDetailsRepository;
import com.Infinitio.kyc.repository.TbRoleMasterRepository;
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
public class RolesService {

    private static final Logger logger = LoggerFactory.getLogger(RolesService.class);


    @Autowired
    private TbRoleDetailsRepository roleDetailsRepository;

    @Autowired
    private DTOService dtoService;



    @Autowired
    private TbRoleMasterRepository roleRepository;


    public List<RoleDTO> getAllRoles() {
        try {
            logger.info("Fetching all roles");
            List<TbRoleMaster> roles = roleRepository.findAll();

            if (roles.isEmpty()) {
                logger.info("No roles found");
                return Collections.emptyList();
            }

            List<RoleDTO> roleDTOs = roles.stream()
                    .filter(Objects::nonNull)
                    .map(dtoService::convertRoleToDTO)
                    .collect(Collectors.toList());

            logger.info("Found {} roles", roleDTOs.size());
            return roleDTOs;

        } catch (Exception e) {
            logger.error("Error while fetching roles: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch roles", e);
        }
    }

    public List<RoleFormDTO> getFormsByRoleId(Integer roleId) {
        logger.info("Fetching forms for role ID: {}", roleId);

        List<TbRoleDetails> roleDetailsList = roleDetailsRepository.findByRoleId(roleId);
        if (roleDetailsList.isEmpty()) {
            logger.warn("No forms found for role ID: {}", roleId);
            return Collections.emptyList();
        }

        return roleDetailsList.stream()
                .map(dtoService::convertRoleDetailToRoleFormDTO)
                .collect(Collectors.toList());
    }


}
