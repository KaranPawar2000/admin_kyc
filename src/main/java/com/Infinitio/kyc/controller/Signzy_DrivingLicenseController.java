package com.Infinitio.kyc.controller;


import com.Infinitio.kyc.dto.DrivingLicenseRequest;
import com.Infinitio.kyc.service.DrivingLicenseService;
import com.Infinitio.kyc.service.Signzy_DrivingLicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/signzy/driving-license")
public class Signzy_DrivingLicenseController {
    private final Signzy_DrivingLicenseService drivingLicenseService;

    public Signzy_DrivingLicenseController(Signzy_DrivingLicenseService drivingLicenseService) {
        this.drivingLicenseService = drivingLicenseService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyDrivingLicense(
            @RequestBody DrivingLicenseRequest request,
            @RequestHeader("apiKey") String apiKey) {

        Map<String, Object> response = drivingLicenseService.verifyDrivingLicense(request, apiKey);
        return ResponseEntity.ok(response);
    }

}
