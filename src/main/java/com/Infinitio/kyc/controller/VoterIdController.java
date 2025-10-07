package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.VoterIdRequest;
import com.Infinitio.kyc.service.VoterIdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/voter-id")
public class VoterIdController {

    private final VoterIdService voterIdService;

    public VoterIdController(VoterIdService voterIdService) {
        this.voterIdService = voterIdService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyVoterId(@RequestBody VoterIdRequest request,
                                                             @RequestHeader("apiKey") String apiKey) {
        Map<String, Object> response = voterIdService.verifyVoterId(request, apiKey);
        return ResponseEntity.ok(response);
    }
}
