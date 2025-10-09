package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.CorporateGstinRequest;
import com.Infinitio.kyc.service.CorporateGstinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/corporate/gstin")
public class CorporateGstinController {

    private final CorporateGstinService corporateGstinService;

    public CorporateGstinController(CorporateGstinService corporateGstinService) {
        this.corporateGstinService = corporateGstinService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCorporateGstin(
            @RequestBody CorporateGstinRequest request,
            @RequestHeader("apiKey") String apiKey) {

        Map<String, Object> response = corporateGstinService.verifyCorporateGstin(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
