package com.Infinitio.kyc.utils;

import com.Infinitio.kyc.dto.ClientDTO;
import com.Infinitio.kyc.entity.TbClientMaster;
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
}
