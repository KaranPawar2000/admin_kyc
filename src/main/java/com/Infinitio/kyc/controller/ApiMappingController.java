package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.ApiTypeRouteDTO;
import com.Infinitio.kyc.service.ApiMappingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiMappingController {

    private final ApiMappingService mappingService;

    public ApiMappingController(ApiMappingService mappingService) {
        this.mappingService = mappingService;
    }

    // Example: GET /api/mappings/1
    @GetMapping("/{clientUserId}")
    public List<ApiTypeRouteDTO> getMappings(@PathVariable Integer clientUserId) {
        return mappingService.getMappingsByClientUserId(clientUserId);
    }
}
