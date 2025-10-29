package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CorporateGstinRequest {

    @JsonProperty("GST_NO")
    private String idNumber;  // corresponds to "id_number"
}
