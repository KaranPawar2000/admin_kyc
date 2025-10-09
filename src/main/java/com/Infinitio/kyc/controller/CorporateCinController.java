package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.CorporateCinRequest;
import com.Infinitio.kyc.service.CorporateCinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/corporate")
public class CorporateCinController {

    private final CorporateCinService corporateCinService;

    public CorporateCinController(CorporateCinService corporateCinService) {
        this.corporateCinService = corporateCinService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCIN(@RequestBody CorporateCinRequest request,
                                                         @RequestHeader("apiKey") String apiKey) {
        System.out.println("Received Corporate CIN verification request: " + request.getIdNumber() + " with apiKey: " + apiKey);
        Map<String, Object> response = corporateCinService.verifyCIN(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
