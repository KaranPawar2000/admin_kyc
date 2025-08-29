package com.Infinitio.kyc.controller;


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
    public ResponseEntity<Map<String, Object>> initialize(
            @RequestParam(name = "signupFlow", defaultValue = "true") boolean signupFlow,
            @RequestParam(name = "skipMainScreen", defaultValue = "false") boolean skipMainScreen,
            @RequestParam(name = "logoUrl", required = false) String logoUrl
    ) {
        Map<String, Object> response = aadhaarService.initializeDigilocker(signupFlow, skipMainScreen, logoUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * After verification on the SDK, call this with the client_id you receive
     * to download the Aadhaar XML and metadata. No webhook needed.
     */
    @GetMapping("/download/{clientId}")
    public ResponseEntity<Map<String, Object>> download(@PathVariable("clientId") @NotBlank String clientId) {
        Map<String, Object> response = aadhaarService.downloadAadhaar(clientId);
        return ResponseEntity.ok(response);
    }
}
