package com.Infinitio.kyc.controller;


import com.Infinitio.kyc.dto.AadhaarInitializeRequest;
import com.Infinitio.kyc.service.AadhaarService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/aadhaar")
@Validated
public class AadharController {

    private final AadhaarService aadhaarService;

    public AadharController(AadhaarService aadhaarService) {
        this.aadhaarService = aadhaarService;
    }

    /**
     * Initialize the Digilocker flow.
     * Your frontend can use the returned token (if needed by SDK) and client_id.
     */

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody AadhaarInitializeRequest request,@RequestHeader("apiKey") String apiKey) {
        Map<String, Object> response = aadhaarService.initializeDigilocker(
                request,apiKey
        );
        return ResponseEntity.ok(response);
    }

    /**
     * After verification on the SDK, call this with the client_id you receive
     * to download the Aadhaar XML and metadata. No webhook needed.
     */

    @GetMapping("/download/{clientId}")
    public ResponseEntity<Map<String, Object>> download(@PathVariable("clientId") @NotBlank String clientId, @RequestHeader("apiKey") String apiKey) {
        Map<String, Object> response = aadhaarService.downloadAadhaar(clientId,apiKey);
        return ResponseEntity.ok(response);
    }
}
