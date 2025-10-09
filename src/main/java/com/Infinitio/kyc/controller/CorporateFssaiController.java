package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.CorporateFssaiRequest;
import com.Infinitio.kyc.service.CorporateFssaiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/corporate/fssai")
public class CorporateFssaiController {

    private final CorporateFssaiService corporateFssaiService;

    public CorporateFssaiController(CorporateFssaiService corporateFssaiService) {
        this.corporateFssaiService = corporateFssaiService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCorporateFssai(
            @RequestBody CorporateFssaiRequest request,
            @RequestHeader("apiKey") String apiKey) {
        System.out.println("Requested ID Number: " + request.getIdNumber());
        Map<String, Object> response = corporateFssaiService.verifyCorporateFssai(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
