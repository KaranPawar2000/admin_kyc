package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CorporateGstinRequest {

    @JsonProperty("id_number")
    private String idNumber;  // corresponds to "id_number"
}
