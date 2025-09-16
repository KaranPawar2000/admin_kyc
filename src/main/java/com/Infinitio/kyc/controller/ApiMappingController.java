package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.ApiTypeRouteDTO;
import com.Infinitio.kyc.dto.ApiTypeRouteUpdateDTO;
import com.Infinitio.kyc.service.ApiMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mappings")
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

    @PutMapping("/update-routes")
    public ResponseEntity<String> updateRoutes(@RequestBody List<ApiTypeRouteUpdateDTO> updates) {
        mappingService.updateRoutesForApiTypes(updates);
        return ResponseEntity.ok("Routes updated successfully");
    }

    @PutMapping("/update-routes/{clientUserId}")
    public ResponseEntity<String> updateRoutes(@PathVariable Integer clientUserId,
                                               @RequestBody List<ApiTypeRouteUpdateDTO> updates) {
        System.out.println("Updating routes for clientUserId: " + clientUserId);
        mappingService.updateRoutesForClient(clientUserId, updates);
        return ResponseEntity.ok("Routes updated successfully for client " + clientUserId);
    }



}
