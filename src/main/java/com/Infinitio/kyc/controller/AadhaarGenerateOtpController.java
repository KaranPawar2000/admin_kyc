package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.AadhaarGenerateOtpRequest;
import com.Infinitio.kyc.service.AadhaarGenerateOtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/aadhaar")
public class AadhaarGenerateOtpController {

    private final AadhaarGenerateOtpService aadhaarGenerateOtpService;

    public AadhaarGenerateOtpController(AadhaarGenerateOtpService aadhaarGenerateOtpService) {
        this.aadhaarGenerateOtpService = aadhaarGenerateOtpService;
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<Map<String, Object>> generateOtp(
            @RequestBody AadhaarGenerateOtpRequest request,
            @RequestHeader("apiKey") String apiKey) {

        Map<String, Object> response = aadhaarGenerateOtpService.generateOtp(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
