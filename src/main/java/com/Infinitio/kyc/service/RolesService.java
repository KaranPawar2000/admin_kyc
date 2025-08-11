package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.RoleDTO;
import com.Infinitio.kyc.dto.RoleFormDTO;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbRoleDetails;
import com.Infinitio.kyc.entity.TbRoleMaster;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
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


    @Autowired
    private TbClientMasterRepository clientMasterRepository;

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




    public RoleDTO addRole(RoleDTO roleDTO) {
        logger.info("Adding new role: {}", roleDTO.getName());

        TbRoleMaster role = new TbRoleMaster();
        role.setName(roleDTO.getName());
        role.setShortCode(roleDTO.getShortCode());
        role.setParentRoleId(roleDTO.getParentRoleId());
        role.setStatus(roleDTO.getStatus());
        role.setSequenceNo(roleDTO.getSequenceNo());
        role.setParentId(roleDTO.getParentId());
        role.setParentName(roleDTO.getParentName());
        role.setType(roleDTO.getType());
        role.setDescription(roleDTO.getDescription());
        role.setReadOnly(roleDTO.getReadOnly());
        role.setArchiveFlag(roleDTO.getArchiveFlag());
        role.setStartPage(roleDTO.getStartPage());
        role.setCreatedModifiedDate(java.time.LocalDateTime.now());

        TbClientMaster client = clientMasterRepository.findById(roleDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + roleDTO.getClientId()));
        role.setClient(client);

        TbRoleMaster savedRole = roleRepository.save(role);
        logger.info("Role saved successfully with ID {}", savedRole.getId());

        return dtoService.convertRoleToDTO(savedRole);  // convert saved entity back to RoleDTO
    }


    public RoleDTO updateRole(Integer id, RoleDTO roleDTO) {
        TbRoleMaster role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));

        role.setName(roleDTO.getName());
        role.setShortCode(roleDTO.getShortCode());
        role.setParentRoleId(roleDTO.getParentRoleId());
        role.setStatus(roleDTO.getStatus());
        role.setSequenceNo(roleDTO.getSequenceNo());
        role.setParentId(roleDTO.getParentId());
        role.setParentName(roleDTO.getParentName());
        role.setType(roleDTO.getType());
        role.setDescription(roleDTO.getDescription());
        role.setReadOnly(roleDTO.getReadOnly());
        role.setArchiveFlag(roleDTO.getArchiveFlag());
        role.setStartPage(roleDTO.getStartPage());
        role.setCreatedModifiedDate(java.time.LocalDateTime.now());

        TbClientMaster client = clientMasterRepository.findById(roleDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + roleDTO.getClientId()));
        role.setClient(client);

        TbRoleMaster updatedRole = roleRepository.save(role);
        return dtoService.convertRoleToDTO(updatedRole);
    }


}
