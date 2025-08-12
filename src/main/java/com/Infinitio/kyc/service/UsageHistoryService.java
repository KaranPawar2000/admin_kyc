// UsageHistoryService.java
package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.UsageHistoryDTO;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.exception.OurException;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsageHistoryService {
    @Autowired
    private TbUsageHistoryRepository usageHistoryRepository;

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