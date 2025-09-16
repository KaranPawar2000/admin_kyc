package com.Infinitio.kyc.dto;

import lombok.Data;

@Data
public class ApiTypeRouteUpdateDTO {
    private Integer apiTypeId;
    private String apiTypeName; // not required for update, but good for debugging
    private Integer routeId;
    private String routeName;   // optional
}
