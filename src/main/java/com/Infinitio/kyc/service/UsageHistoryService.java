// UsageHistoryService.java
package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.ClientApiUsageCount;
import com.Infinitio.kyc.dto.UsageHistoryDTO;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.exception.OurException;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsageHistoryService {
    @Autowired
    private TbUsageHistoryRepository usageHistoryRepository;

    public UsageHistoryService(TbUsageHistoryRepository usageHistoryRepository) {
        this.usageHistoryRepository = usageHistoryRepository;
    }

    public List<UsageHistoryDTO> getAllUsageHistory() {
        List<TbUsageHistory> usageHistories = usageHistoryRepository.findAll();
        if (usageHistories.isEmpty()) {
            throw new OurException("No usage history records found");
        }
        return usageHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public UsageHistoryDTO getUsageHistoryById(Integer id) {
        TbUsageHistory history = usageHistoryRepository.findById(id)
                .orElseThrow(() -> new OurException("Usage history not found with id: " + id));

        return convertToDTO(history);
    }


    public List<ClientApiUsageCount> getClientWiseApiUsageCount() {
        List<Object[]> results = usageHistoryRepository.getClientWiseApiUsageCount();
        Map<Integer, ClientApiUsageCount> clientMap = new HashMap<>();

        for (Object[] row : results) {
            Integer clientId = ((Number) row[0]).intValue();
            String clientName = (String) row[1];
            String apiName = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            ClientApiUsageCount clientCount = clientMap.computeIfAbsent(
                    clientId,
                    k -> new ClientApiUsageCount(clientId, clientName)
            );

            clientCount.getApiCounts().put(apiName, count);
        }

        return new ArrayList<>(clientMap.values());
    }

    public ClientApiUsageCount getClientWiseApiUsageCountById(Integer clientId) {
        List<Object[]> results = usageHistoryRepository.getClientWiseApiUsageCountById(clientId);

        if (results.isEmpty()) {
            throw new OurException("No usage history found for clientId: " + clientId);
        }

        ClientApiUsageCount clientCount = null;

        for (Object[] row : results) {
            Integer cId = ((Number) row[0]).intValue();
            String clientName = (String) row[1];
            String apiName = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            if (clientCount == null) {
                clientCount = new ClientApiUsageCount(cId, clientName);
            }

            clientCount.getApiCounts().put(apiName, count);
        }

        return clientCount;
    }

    public ClientApiUsageCount getClientWiseApiUsageCountByIdLast30Days(Integer clientId) {
        List<Object[]> results = usageHistoryRepository.getClientWiseApiUsageCountByIdLast30Days(clientId);

        if (results.isEmpty()) {
            throw new OurException("No usage history found in the last 30 days for clientId: " + clientId);
        }

        ClientApiUsageCount clientCount = null;

        for (Object[] row : results) {
            Integer cId = ((Number) row[0]).intValue();
            String clientName = (String) row[1];
            String apiName = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            if (clientCount == null) {
                clientCount = new ClientApiUsageCount(cId, clientName);
            }

            clientCount.getApiCounts().put(apiName, count);
        }

        return clientCount;
    }

    // ✅ New Method: With startDate
    public ClientApiUsageCount getClientWiseApiUsageCountByIdFromDate(Integer clientId, LocalDateTime startDate) {
        List<Object[]> results = usageHistoryRepository.getClientWiseApiUsageCountByIdFromDate(clientId, startDate);

        if (results.isEmpty()) {
            throw new OurException("No usage history found for clientId: " + clientId + " from date: " + startDate);
        }

        ClientApiUsageCount clientCount = null;

        for (Object[] row : results) {
            Integer cId = ((Number) row[0]).intValue();
            String clientName = (String) row[1];
            String apiName = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            if (clientCount == null) {
                clientCount = new ClientApiUsageCount(cId, clientName);
            }

            clientCount.getApiCounts().put(apiName, count);
        }

        return clientCount;
    }

    public List<UsageHistoryDTO> getUsageHistoryByClientId(Integer clientId) {
        List<TbUsageHistory> usageHistories = usageHistoryRepository.findByClientId(clientId);

        if (usageHistories.isEmpty()) {
            throw new OurException("No usage history found for clientId: " + clientId);
        }

        return usageHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    private UsageHistoryDTO convertToDTO(TbUsageHistory history) {
        UsageHistoryDTO dto = new UsageHistoryDTO();
        dto.setId(history.getId());
        dto.setSentTime(history.getSentTime());
        dto.setApiTypeName(history.getApiType() != null ? history.getApiType().getName() : null);
        dto.setApiRequestBody(history.getApiRequestBody());
        dto.setApiResponseBody(history.getApiResponseBody());
        dto.setStatus(history.getStatus());
        dto.setMessage(history.getMessage());
        dto.setMessageCode(history.getMessageCode());
        dto.setData(history.getData());
        dto.setCreatedModifiedDate(history.getCreatedModifiedDate());
        dto.setReadOnly(history.getReadOnly());
        dto.setArchiveFlag(history.getArchiveFlag());
        dto.setClientName(history.getClient() != null ? history.getClient().getOrgName() : null);
        dto.setVendorRequestBody(history.getVendorRequestBody());
        return dto;
    }


}