package com.Infinitio.kyc.controller;
import com.Infinitio.kyc.dto.VoterIdRequest;
import com.Infinitio.kyc.service.Signzy_VoterIdService;
import com.Infinitio.kyc.service.VoterIdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

    @RestController
    @RequestMapping("/api/signzy/voterid")
    public class Signzy_VoterIdController {

        private final Signzy_VoterIdService voterIdService;

        public Signzy_VoterIdController(Signzy_VoterIdService voterIdService) {
            this.voterIdService = voterIdService;
        }

        @PostMapping("/verify")
        public ResponseEntity<Map<String, Object>> verifyVoterId(@RequestBody VoterIdRequest request,
                                                                 @RequestHeader("apiKey") String apiKey) {
            Map<String, Object> response = voterIdService.verifyVoterId(request, apiKey);
            return ResponseEntity.ok(response);
        }
    }

