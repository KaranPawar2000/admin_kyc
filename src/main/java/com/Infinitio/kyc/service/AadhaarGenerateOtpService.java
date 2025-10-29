package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.AadhaarGenerateOtpRequest;
import com.Infinitio.kyc.entity.TbApiTypeMaster;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.repository.TbApiTypeMasterRepository;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AadhaarGenerateOtpService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public AadhaarGenerateOtpService(WebClient webClient,
                                     TbClientMasterRepository clientRepo,
                                     TbUsageHistoryRepository usageRepo,
                                     TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> generateOtp(AadhaarGenerateOtpRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null)
            throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/aadhaar-v2/generate-otp";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id_number", request.getAADHAR_NO());

        Map<String, Object> apiResponse = new LinkedHashMap<>();
        Map<String, Object> formattedResponse = new LinkedHashMap<>();

        int status = 1;
        String message = "Success";

        try {
            // 🔹 Call Surepass API
            apiResponse = webClient.post()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            formattedResponse = formatResponse(apiResponse);

        } catch (WebClientResponseException e) {
            status = 0;
            message = "Surepass API Error: " + e.getStatusCode().value();

            apiResponse.put("status", e.getStatusCode().value());
            apiResponse.put("error", e.getResponseBodyAsString());

            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "message", "API_ERROR",
                    "data", Map.of("result", Map.of("error", e.getResponseBodyAsString()))
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();

            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            formattedResponse = Map.of(
                    "code", 500,
                    "message", "INTERNAL_SERVER_ERROR",
                    "data", Map.of("result", Map.of("error", e.getMessage()))
            );
        } finally {
            // 🔹 Save usage log
            try {
                TbUsageHistory log = new TbUsageHistory();
                log.setApiRequestBody(mapper.writeValueAsString(request));
                log.setVendorRequestBody(mapper.writeValueAsString(payload));
                log.setApiResponseBody(mapper.writeValueAsString(apiResponse));
                log.setData(mapper.writeValueAsString(formattedResponse.get("data")));
                log.setStatus(status);
                log.setMessage(message);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                // ✅ Change API type ID as per your DB (example: 9 for Aadhaar OTP)
                TbApiTypeMaster apiType = apiTypeRepo.findById(9)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 9 not found"));
                log.setApiType(apiType);

                usageRepo.save(log);

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing usage log", e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return formattedResponse;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> formatResponse(Map<String, Object> apiResponse) {
        Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");
        if (data == null) data = new LinkedHashMap<>();

        // 🔹 Convert Surepass response to your desired format
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestId", data.get("client_id"));
        result.put("otp_status", data.get("otp_sent"));
        result.put("if_number", data.get("if_number"));
        result.put("isValidAadhaar", data.get("valid_aadhaar"));
        result.put("status", data.get("status"));

        Map<String, Object> finalData = new LinkedHashMap<>();
        finalData.put("result", result);

        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("code", apiResponse.getOrDefault("status_code", 200));
        finalResponse.put("message", apiResponse.getOrDefault("message_code", "success"));
        finalResponse.put("data", finalData);

        return finalResponse;
    }
}
