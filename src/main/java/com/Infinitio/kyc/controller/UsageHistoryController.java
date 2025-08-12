package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.UsageHistoryDTO;
import com.Infinitio.kyc.service.UsageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usage-history")
public class UsageHistoryController {

    @Autowired
    private UsageHistoryService usageHistoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsageHistory() {
        List<UsageHistoryDTO> historyList = usageHistoryService.getAllUsageHistory();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Success");
        response.put("data", historyList);

        return ResponseEntity.ok(response);
    }
}