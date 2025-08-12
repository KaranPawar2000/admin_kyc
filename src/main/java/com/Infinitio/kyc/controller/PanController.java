package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.PanRequest;
import com.Infinitio.kyc.dto.PanResponse;
import com.Infinitio.kyc.service.PanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pan")
public class PanController {

    @Autowired
    private PanService panService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPan(@RequestBody PanRequest panRequest, @RequestHeader("apiKey") String apiKey) {
        PanResponse response = panService.verifyAndPersist(panRequest, apiKey);
        return ResponseEntity.ok(response);
    }
}
