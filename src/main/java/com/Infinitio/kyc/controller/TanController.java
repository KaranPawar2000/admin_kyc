package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.TanRequest;
import com.Infinitio.kyc.service.TanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tan")
public class TanController {

    private final TanService tanService;

    public TanController(TanService tanService) {
        this.tanService = tanService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyTan(@RequestBody TanRequest request,
                                                         @RequestHeader("apiKey") String apiKey) {

        System.out.println("Received TAN verification request: " + request.getIdNumber() + " with apiKey: " + apiKey);
        Map<String, Object> response = tanService.verifyTan(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
