package com.Infinitio.kyc.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiTypeRouteDTO {

    private Integer apiTypeId;
    private String apiTypeName;
    private Integer routeId;
    private String routeName;
}
