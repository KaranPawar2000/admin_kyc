package com.Infinitio.kyc.dto;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class ClientApiUsageCount {
    private Integer clientId;
    private String clientName;
    private Map<String, Long> apiCounts;

    public ClientApiUsageCount(Integer clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.apiCounts = new HashMap<>();
    }
}