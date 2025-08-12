package com.Infinitio.kyc.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsageHistoryDTO {
    private Integer id;
    private LocalDateTime sentTime;
    private String apiTypeName;
    private String apiRequestBody;
    private String apiResponseBody;
    private Integer status;
    private String message;
    private String messageCode;
    private String data;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;
    private String clientName;
    private String vendorRequestBody;
}