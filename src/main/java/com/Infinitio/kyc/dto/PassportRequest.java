package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PassportRequest {

    @JsonProperty("id_number")
    private String idNumber;

    @JsonProperty("dob")
    private String dob; // expected in ISO format: "1996-04-20"
}
