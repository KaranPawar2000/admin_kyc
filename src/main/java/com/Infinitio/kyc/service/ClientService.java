package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.ClientDTO;
import com.Infinitio.kyc.dto.ClientDTOAdd;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbRoleMaster;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.utils.DTOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private TbClientMasterRepository clientRepository;

    @Autowired
    private DTOService dtoService;


    public List<ClientDTO> getAllClients() {
        try {
            logger.info("Fetching all clients");
            List<TbClientMaster> clients = clientRepository.findAll();

            if (clients.isEmpty()) {
                logger.info("No clients found");
                return Collections.emptyList();
            }

            List<ClientDTO> clientDTOs = clients.stream()
                    .filter(Objects::nonNull)
                    .map(dtoService::convertClientToDTO)
                    .collect(Collectors.toList());

            logger.info("Found {} clients", clientDTOs.size());
            return clientDTOs;

        } catch (Exception e) {
            logger.error("Error while fetching clients: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch clients", e);
        }
    }

    public ClientDTO getClientById(Integer id) {
        try {
            logger.info("Fetching client with id: {}", id);
            return clientRepository.findById(id)
                    .map(dtoService::convertClientToDTO)
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        } catch (Exception e) {
            logger.error("Error while fetching client with id ", id, e.getMessage());
            throw new RuntimeException("Failed to fetch client", e);
        }
    }


    public ClientDTOAdd addClient(ClientDTOAdd clientDTO) {
        try {
            TbClientMaster client = new TbClientMaster();

            // Set basic fields
            client.setOrgName(clientDTO.getOrgName());
            client.setEmailId(clientDTO.getEmailId());
            client.setMobileNo(clientDTO.getMobileNo());
            client.setPassword(clientDTO.getPassword()); // consider hashing
            client.setExpiryDate(clientDTO.getExpiryDate());
            client.setClientCount(clientDTO.getClientCount());
            client.setAddress(clientDTO.getAddress());
            client.setLogo(clientDTO.getLogo());
            client.setPincode(clientDTO.getPincode());
            client.setStatus(clientDTO.getStatus());
            client.setIsEncrypted(clientDTO.getIsEncrypted());
            client.setCreatedModifiedDate(java.time.LocalDateTime.now());
            client.setClientId(1);

            TbRoleMaster role = new TbRoleMaster();
            role.setId(3);                          // Assuming 3 is the role ID for clients
            client.setRole(role);                  // setting the role into the client
            client.setReadOnly("N"); // Assuming readOnly is a string field
            client.setArchiveFlag("F"); // Assuming archiveFlag is a string field

            //Generate secure API key
            String apiKey = generateSecureApiKey(clientDTO.getEmailId(), clientDTO.getPassword());
            client.setApiKey(apiKey);

            // Save entity
            TbClientMaster saved = clientRepository.save(client);

            // Optionally return the same DTO back (excluding password)
            ClientDTOAdd responseDTO = new ClientDTOAdd();
            responseDTO.setOrgName(saved.getOrgName());
            responseDTO.setEmailId(saved.getEmailId());
            responseDTO.setMobileNo(saved.getMobileNo());
            responseDTO.setExpiryDate(saved.getExpiryDate());
            responseDTO.setClientCount(saved.getClientCount());
            responseDTO.setAddress(saved.getAddress());
            responseDTO.setLogo(saved.getLogo());
            responseDTO.setPincode(saved.getPincode());
            responseDTO.setStatus(saved.getStatus());
            responseDTO.setIsEncrypted(saved.getIsEncrypted());
            responseDTO.setApiKey(saved.getApiKey());
            responseDTO.setReadOnly(saved.getReadOnly());
            responseDTO.setArchiveFlag(saved.getArchiveFlag());
            responseDTO.setRoleId(saved.getRole().getId());
            responseDTO.setRoleName(saved.getRole().getName());
            return responseDTO;

        } catch (Exception e) {
            logger.error("Error while adding client: {}", e.getMessage());
            throw new RuntimeException("Failed to add client", e);
        }
    }


    public ClientDTOAdd updateClient(Integer id, ClientDTOAdd clientDTO) {
        // Validate mandatory fields
        if (clientDTO.getOrgName() == null || clientDTO.getOrgName().trim().isEmpty() ||
                clientDTO.getEmailId() == null || clientDTO.getEmailId().trim().isEmpty() ||
                clientDTO.getMobileNo() == null || clientDTO.getMobileNo().trim().isEmpty() ||
                clientDTO.getPassword() == null || clientDTO.getPassword().trim().isEmpty() ||
                clientDTO.getExpiryDate() == null ||
                clientDTO.getClientCount() == null ||
                clientDTO.getAddress() == null || clientDTO.getAddress().trim().isEmpty() ||
                clientDTO.getLogo() == null || clientDTO.getLogo().trim().isEmpty() ||
                clientDTO.getPincode() == null || clientDTO.getPincode().trim().isEmpty() ||
                clientDTO.getStatus() == null ||
                clientDTO.getIsEncrypted() == null || // Ensure isEncrypted is Byte, not byte
                clientDTO.getRoleId() == null
        ) {
            throw new RuntimeException("All fields are mandatory for update");
        }

        try {
            TbClientMaster existingClient = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

            existingClient.setOrgName(clientDTO.getOrgName());
            existingClient.setEmailId(clientDTO.getEmailId());
            existingClient.setMobileNo(clientDTO.getMobileNo());
            existingClient.setPassword(clientDTO.getPassword());
            existingClient.setExpiryDate(clientDTO.getExpiryDate());
            existingClient.setClientCount(clientDTO.getClientCount());
            existingClient.setAddress(clientDTO.getAddress());
            existingClient.setLogo(clientDTO.getLogo());
            existingClient.setPincode(clientDTO.getPincode());
            existingClient.setStatus(clientDTO.getStatus());
            existingClient.setIsEncrypted(clientDTO.getIsEncrypted());
            existingClient.setCreatedModifiedDate(java.time.LocalDateTime.now());
            existingClient.setReadOnly("N");
            existingClient.setArchiveFlag("F");

            TbRoleMaster role = new TbRoleMaster();
            role.setId(clientDTO.getRoleId());
            existingClient.setRole(role);

            String newApiKey = generateSecureApiKey(clientDTO.getEmailId(), clientDTO.getPassword());
            existingClient.setApiKey(newApiKey);

            TbClientMaster updated = clientRepository.save(existingClient);

            ClientDTOAdd responseDTO = new ClientDTOAdd();
            responseDTO.setOrgName(updated.getOrgName());
            responseDTO.setEmailId(updated.getEmailId());
            responseDTO.setMobileNo(updated.getMobileNo());
            responseDTO.setExpiryDate(updated.getExpiryDate());
            responseDTO.setClientCount(updated.getClientCount());
            responseDTO.setAddress(updated.getAddress());
            responseDTO.setLogo(updated.getLogo());
            responseDTO.setPincode(updated.getPincode());
            responseDTO.setStatus(updated.getStatus());
            responseDTO.setIsEncrypted(updated.getIsEncrypted());
            responseDTO.setApiKey(updated.getApiKey());
            responseDTO.setReadOnly(updated.getReadOnly());
            responseDTO.setArchiveFlag(updated.getArchiveFlag());
            responseDTO.setRoleId(updated.getRole().getId());
            responseDTO.setRoleName(updated.getRole().getName());

            return responseDTO;

        } catch (Exception e) {
            logger.error("Error updating client with id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update client", e);
        }
    }



    private String generateSecureApiKey(String email, String password) {
        String raw = email + ":" + password + ":" + System.currentTimeMillis();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating API key", e);
        }
    }



}
