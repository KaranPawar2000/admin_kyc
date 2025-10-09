package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.CorporateGstinRequest;
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
import java.util.*;

@Service
public class CorporateGstinService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public CorporateGstinService(WebClient webClient,
                                 TbClientMasterRepository clientRepo,
                                 TbUsageHistoryRepository usageRepo,
                                 TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyCorporateGstin(CorporateGstinRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null)
            throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/corporate/gstin";

        Map<String, Object> payload = Map.of("id_number", request.getIdNumber());
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> formattedResponse = new HashMap<>();

        int status = 1;
        String message = "Success";

        try {
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
            // Always log usage
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

                // Change ID if needed (example: 10 for GSTIN API)
                TbApiTypeMaster apiType = apiTypeRepo.findById(10)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 10 not found"));
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
        if (data == null) {
            return Map.of(
                    "code", 500,
                    "data", Map.of("result", Map.of("error", "Empty response from Surepass"))
            );
        }

        // Utility function to safely convert to string
        java.util.function.Function<Object, String> toStr = v -> v == null ? "" : v.toString();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gstin", toStr.apply(data.get("gstin")));
        result.put("pan_number", toStr.apply(data.get("pan_number")));
        result.put("business_name", toStr.apply(data.get("business_name")));
        result.put("legal_name", toStr.apply(data.get("legal_name")));
        result.put("date_of_registration", toStr.apply(data.get("date_of_registration")));
        result.put("constitution_of_business", toStr.apply(data.get("constitution_of_business")));
        result.put("taxpayer_type", toStr.apply(data.get("taxpayer_type")));
        result.put("gstin_status", toStr.apply(data.get("gstin_status")));
        result.put("date_of_cancellation", toStr.apply(data.get("date_of_cancellation")));
        result.put("field_visit_conducted", toStr.apply(data.get("field_visit_conducted")));
        result.put("nature_bus_activities", data.get("nature_bus_activities"));
        result.put("nature_of_core_business_activity_description",
                toStr.apply(data.get("nature_of_core_business_activity_description")));
        result.put("aadhaar_validation", toStr.apply(data.get("aadhaar_validation")));
        result.put("address", toStr.apply(data.get("address")));
        result.put("aadhaar_validation_date", toStr.apply(data.get("aadhaar_validation_date")));
        result.put("center_jurisdiction", toStr.apply(data.get("center_jurisdiction")));
        result.put("state_jurisdiction", toStr.apply(data.get("state_jurisdiction")));

        return Map.of(
                "code", 200,
                "message", "Success",
                "data", result
        );
    }
}
