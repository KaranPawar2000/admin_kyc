package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.AadhaarSubmitOtpRequest;
import com.Infinitio.kyc.entity.TbApiTypeMaster;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.repository.TbApiTypeMasterRepository;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AadhaarSubmitOtpService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public AadhaarSubmitOtpService(WebClient webClient,
                                   TbClientMasterRepository clientRepo,
                                   TbUsageHistoryRepository usageRepo,
                                   TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> submitOtp(AadhaarSubmitOtpRequest request, String apiKey) {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> formattedResponse = new LinkedHashMap<>();
        Map<String, Object> apiResponse = new LinkedHashMap<>();

        int status = 1;
        String message = "Success";

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) {
            throw new RuntimeException("Client not found for apiKey: " + apiKey);
        }

        String url = baseUrl + "/api/v1/aadhaar-v2/submit-otp";

        // ✅ Build payload exactly as required by Surepass
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("client_id", request.getClient_id());
        payload.put("otp", request.getOtp());


        try {
            apiResponse = webClient.post()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("API Response from surepass:- "+apiResponse);
            formattedResponse = formatResponse(apiResponse);
            System.out.println("API Response from surepass:- "+apiResponse);
        } catch (WebClientResponseException e) {
            status = 0;
            message = "Surepass API Error: " + e.getStatusCode().value();

            apiResponse.put("status", e.getStatusCode().value());
            apiResponse.put("error", e.getResponseBodyAsString());

            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "data", Map.of("result", Map.of("error", e.getResponseBodyAsString()))
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();

            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            formattedResponse = Map.of(
                    "code", 500,
                    "data", Map.of("result", Map.of("error", e.getMessage()))
            );
        } finally {
            try {
                TbUsageHistory log = new TbUsageHistory();
                log.setApiRequestBody(mapper.writeValueAsString(request));
                log.setVendorRequestBody(mapper.writeValueAsString(request));
                log.setApiResponseBody(mapper.writeValueAsString(apiResponse));
                log.setData(mapper.writeValueAsString(formattedResponse.get("data")));
                log.setStatus(status);
                log.setMessage(message);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                // Assuming API Type ID = 9 for Aadhaar Submit OTP
                TbApiTypeMaster apiType = apiTypeRepo.findById(9)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 9 not found"));
                log.setApiType(apiType);

                usageRepo.save(log);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return formattedResponse;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> formatResponse(Map<String, Object> apiResponse) {
        Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");

        if (data == null) {
            return Map.of("code", 500, "data", Map.of("result", Map.of("error", "No data from Surepass")));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fullName", data.get("full_name"));
        result.put("aadhaarNumber", data.get("aadhaar_number"));
        result.put("dob", data.get("dob"));
        result.put("gender", data.get("gender"));
        result.put("zip", data.get("zip"));
        result.put("address", data.get("address"));
//        result.put("faceStatus", data.get("face_status"));
//        result.put("faceScore", data.get("face_score"));
        result.put("profileImage", data.get("profile_image"));

        return Map.of(
                "code", 200,
                "data", Map.of("result", result)
        );
    }
}
