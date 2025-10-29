package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.AadhaarSubmitOtpRequest;
import com.Infinitio.kyc.service.AadhaarSubmitOtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/aadhaar")
public class AadhaarSubmitOtpController {

    private final AadhaarSubmitOtpService submitOtpService;

    public AadhaarSubmitOtpController(AadhaarSubmitOtpService submitOtpService) {
        this.submitOtpService = submitOtpService;
    }

    @PostMapping("/submit-otp")
    public ResponseEntity<Map<String, Object>> submitOtp(
            @RequestBody AadhaarSubmitOtpRequest request,
            @RequestHeader("apiKey") String apiKey) {

        System.out.println("Received submit OTP request: " + request.getClient_id() + ", OTP: " + request.getOtp());
        Map<String, Object> response = submitOtpService.submitOtp(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
