package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.ClientApiUsageCount;
import com.Infinitio.kyc.dto.UsageHistoryDTO;
import com.Infinitio.kyc.service.UsageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsageHistory() {
        List<UsageHistoryDTO> historyList = usageHistoryService.getAllUsageHistory();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Success");
        response.put("data", historyList);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUsageHistoryById(@PathVariable Integer id) {
        UsageHistoryDTO history = usageHistoryService.getUsageHistoryById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Success");
        response.put("data", history);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<Map<String, Object>> getUsageHistoryByClientId(@PathVariable Integer clientId) {
        List<UsageHistoryDTO> historyList = usageHistoryService.getUsageHistoryByClientId(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Success");
        response.put("data", historyList);

        return ResponseEntity.ok(response);
    }



    @GetMapping("/client-wise-count")
    public ResponseEntity<List<ClientApiUsageCount>> getClientWiseApiUsageCount() {
        return ResponseEntity.ok(usageHistoryService.getClientWiseApiUsageCount());
    }

    @GetMapping("/client-wise-count/{clientId}")
    public ResponseEntity<Map<String, Object>> getClientWiseApiUsageCountById(@PathVariable Integer clientId) {
        ClientApiUsageCount report = usageHistoryService.getClientWiseApiUsageCountById(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Success");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client-wise-count/{clientId}/last-30-days")
    public ResponseEntity<Map<String, Object>> getClientWiseApiUsageCountByIdLast30Days(@PathVariable Integer clientId) {
        ClientApiUsageCount report = usageHistoryService.getClientWiseApiUsageCountByIdLast30Days(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Success");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }


}