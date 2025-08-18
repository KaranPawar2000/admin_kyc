package com.Infinitio.kyc.dto;

import lombok.Data;

@Data
public class RoleDetailUpdateRequest {
    private Byte showInMenu;
    private Integer seqNo;
    private Byte isAllowed;
}
