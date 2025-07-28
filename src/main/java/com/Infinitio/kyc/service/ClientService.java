package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.ClientDTO;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.utils.DTOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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



}
