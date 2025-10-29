package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AadhaarSubmitOtpRequest {

    @JsonProperty("CLIENT_ID")
    private String client_id;

    @JsonProperty("OTP")
    private String otp;

}
