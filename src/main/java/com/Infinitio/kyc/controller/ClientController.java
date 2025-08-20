package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.*;
import com.Infinitio.kyc.exception.OurException;
import com.Infinitio.kyc.service.ClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @GetMapping("/all")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        logger.info("Received request to fetch all clients");
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Integer id) {
        logger.info("Received request to fetch client with id: {}", id);
        ClientDTO client = clientService.getClientById(id);
        return client != null
                ? ResponseEntity.ok(client)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/add")
    public ResponseEntity<ClientDTOAdd> addClient(@RequestBody ClientDTOAdd clientDTO) {
        logger.info("Received request to add new client with email: {}", clientDTO.getEmailId());
        ClientDTOAdd savedClient = clientService.addClient(clientDTO);
        return ResponseEntity.ok(savedClient);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ClientDTOAdd> updateClient(@PathVariable Integer id, @RequestBody ClientDTOAdd clientDTO) {
        logger.info("Received request to update client with id: {}", id);
        ClientDTOAdd updatedClient = clientService.updateClient(id, clientDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @PostMapping("/login")
    public ResponseEntity<ClientLoginResponse> login(@RequestBody @Valid AdminLoginRequest request) {
        logger.info("Received client login request for email: {}", request.getEmailId());
        try {
            ClientLoginResponse response = clientService.login(request);
            return ResponseEntity.ok(response);
        } catch (OurException e) {
            logger.error("Client login failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during client login: {}", e.getMessage());
            throw new OurException("Internal server error");
        }
    }


    @PatchMapping("/update-password/{id}")
    public ResponseEntity<String> updatePassword(
            @PathVariable Integer id,
            @RequestBody ClientPasswordUpdateRequest request) {
        logger.info("Received request to update password for client id: {}", id);
        try {
            clientService.updatePassword(id, request);
            return ResponseEntity.ok("Password updated successfully");
        } catch (OurException e) {
            logger.error("Password update failed for client id {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while updating password for client id {}: {}", id, e.getMessage());
            throw new OurException("Internal server error");
        }
    }

    @GetMapping("/{id}/api-key")
    public ResponseEntity<String> getApiKeyById(@PathVariable Integer id) {
        logger.info("Received request to fetch API key for client id: {}", id);
        String apiKey = clientService.getApiKeyById(id);
        return apiKey != null
                ? ResponseEntity.ok(apiKey)
                : ResponseEntity.notFound().build();
    }



}