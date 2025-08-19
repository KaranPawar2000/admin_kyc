package com.Infinitio.kyc.utils;

import com.Infinitio.kyc.dto.*;
import com.Infinitio.kyc.entity.*;
import org.springframework.stereotype.Component;


@Component
public class DTOService {
    // This class can be used to implement common DTO conversion logic
    // or utility methods for handling DTOs across the application.

    // Example method to convert an entity to a DTO
     public ClientDTO convertClientToDTO(TbClientMaster client) {
         ClientDTO dto = new ClientDTO();
         dto.setId(client.getId());
         dto.setOrgName(client.getOrgName());
         dto.setEmailId(client.getEmailId());
         dto.setMobileNo(client.getMobileNo());
         dto.setPincode(client.getPincode());
         dto.setIsEncrypted(client.getIsEncrypted());
         dto.setStatus(client.getStatus());
         dto.setAddress(client.getAddress());
         dto.setExpiryDate(client.getExpiryDate());
         dto.setLogo(client.getLogo());
         dto.setApiKey(client.getApiKey());
         return dto;
     }


    public FormDTO convertFormToDTO(TbFormMaster form) {
        FormDTO dto = new FormDTO();
        dto.setId(form.getId());
        dto.setFormName(form.getName());
        dto.setLink(form.getLink());
        return dto;
    }

//    public RoleDTO convertRoleToDTO(TbRoleMaster role) {
//        RoleDTO dto = new RoleDTO();
//        dto.setId(role.getId());
//        dto.setName(role.getName());
//        dto.setStatus(role.getStatus());
//        dto.setDescription(role.getDescription());
//        return dto;
//    }

    public RoleFormDTO convertRoleDetailToRoleFormDTO(TbRoleDetails details) {
        RoleFormDTO dto = new RoleFormDTO();
        dto.setFormName(details.getForm().getName());
        dto.setFormId(details.getForm().getId());
        dto.setFormLink(details.getForm().getLink());
        dto.setCreatedModifiedDate(details.getCreatedModifiedDate());
        dto.setShowInMenu(details.getShowInMenu());
        dto.setSeqNo(details.getSeqNo());
        dto.setIsAllowed(details.getIsAllowed()); // ✅ Set isAllowed value
        return dto;
    }


    public AdminDTO convertAdminToDTO(TbAdminMaster admin) {
        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setName(admin.getName());
        dto.setEmailId(admin.getEmailId());
        dto.setMobileNo(admin.getMobileNo());
        dto.setIsActive(admin.getIsActive());
        dto.setPassword(admin.getPassword());
        dto.setCreatedModifiedDate(admin.getCreatedModifiedDate());
        dto.setReadOnly(admin.getReadOnly());
        dto.setArchiveFlag(admin.getArchiveFlag());

        if (admin.getRole() != null) {
            dto.setRoleId(admin.getRole().getId());
            dto.setRoleName(admin.getRole().getName());
        }

        if (admin.getClient() != null) {
            dto.setClientId(admin.getClient().getId());
        }

        return dto;
    }

    public RoleDTO convertRoleToDTO(TbRoleMaster role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setStatus(role.getStatus());
        dto.setDescription(role.getDescription());
        dto.setShortCode(role.getShortCode());
        dto.setParentRoleId(role.getParentRoleId());
        dto.setSequenceNo(role.getSequenceNo());
        dto.setParentId(role.getParentId());
        dto.setParentName(role.getParentName());
        dto.setType(role.getType());
        dto.setReadOnly(role.getReadOnly());
        dto.setArchiveFlag(role.getArchiveFlag());
        dto.setStartPage(role.getStartPage());

        if (role.getClient() != null) {
            dto.setClientId(role.getClient().getId());
        }

        return dto;
    }





}
