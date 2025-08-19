package com.Infinitio.kyc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleFormDTO {
    private String formName;
    private Integer formId;
    private String formLink;
    private LocalDateTime createdModifiedDate;
    private Byte showInMenu;
    private Integer seqNo;
    private Byte isAllowed;
}

