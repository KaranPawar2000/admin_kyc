package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.ClientDTO;
import com.Infinitio.kyc.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}