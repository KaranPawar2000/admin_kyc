package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.PassportRequest;
import com.Infinitio.kyc.service.PassportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/passport")
public class PassportController {

    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPassport(@RequestBody PassportRequest request,
                                                              @RequestHeader("apiKey") String apiKey) {
        Map<String, Object> response = passportService.verifyPassport(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
