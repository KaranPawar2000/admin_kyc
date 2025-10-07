package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.DrivingLicenseRequest;
import com.Infinitio.kyc.service.DrivingLicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driving-license")
public class DrivingLicenseController {

    private final DrivingLicenseService drivingLicenseService;

    public DrivingLicenseController(DrivingLicenseService drivingLicenseService) {
        this.drivingLicenseService = drivingLicenseService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyDrivingLicense(
            @RequestBody DrivingLicenseRequest request,
            @RequestHeader("apiKey") String apiKey
    ) {
        System.out.println("DOB "+request.getDob()+" NUMBER: "+request.getNumber());
        System.out.println("Received API Key for DL: " + apiKey);
        Map<String, Object> response = drivingLicenseService.verifyDrivingLicense(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
