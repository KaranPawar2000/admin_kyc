package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.ClientDTO;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private TbClientMasterRepository clientRepository;


    public List<ClientDTO> getAllClients() {
        logger.info("Fetching all clients");
        List<TbClientMaster> clients = clientRepository.findAll();
        List<ClientDTO> clientDTOs = clients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("Found {} clients", clientDTOs.size());
        return clientDTOs;
    }

    private ClientDTO convertToDTO(TbClientMaster client) {
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
        dto.setClientCount(client.getClientCount());

        dto.setStatus(client.getStatus());
        return dto;
    }
}
