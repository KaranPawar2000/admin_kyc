package com.Infinitio.kyc.utils;

import com.Infinitio.kyc.dto.ClientDTO;
import com.Infinitio.kyc.dto.FormDTO;
import com.Infinitio.kyc.dto.RoleDTO;
import com.Infinitio.kyc.dto.RoleFormDTO;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbFormMaster;
import com.Infinitio.kyc.entity.TbRoleDetails;
import com.Infinitio.kyc.entity.TbRoleMaster;
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

    public RoleDTO convertRoleToDTO(TbRoleMaster role) {
        RoleDTO dto = new RoleDTO();
        dto.setName(role.getName());
        dto.setStatus(role.getStatus());
        dto.setDescription(role.getDescription());
        return dto;
    }

    public RoleFormDTO convertRoleDetailToRoleFormDTO(TbRoleDetails details) {
        RoleFormDTO dto = new RoleFormDTO();
        dto.setFormName(details.getForm().getName());
        dto.setFormLink(details.getForm().getLink());
        dto.setCreatedModifiedDate(details.getCreatedModifiedDate());
        dto.setShowInMenu(details.getShowInMenu());
        dto.setSeqNo(details.getSeqNo());
        dto.setIsAllowed(details.getIsAllowed()); // ✅ Set isAllowed value
        return dto;
    }




}
