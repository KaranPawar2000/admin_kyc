package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TanRequest {

    @JsonProperty("TAN_NO")
    private String idNumber;
}
