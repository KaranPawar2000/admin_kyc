package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.UdyogAadharRequest;
import com.Infinitio.kyc.service.UdyogAadharService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/udyog-aadhar")
public class UdyogAadharController {

    private final UdyogAadharService udyogAadharService;

    public UdyogAadharController(UdyogAadharService udyogAadharService) {
        this.udyogAadharService = udyogAadharService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyUdyogAadhar(@RequestBody UdyogAadharRequest request,
                                                                 @RequestHeader("apiKey") String apiKey) {
        Map<String, Object> response = udyogAadharService.verifyUdyogAadhar(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
