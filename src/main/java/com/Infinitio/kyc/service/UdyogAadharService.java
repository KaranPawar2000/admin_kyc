package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.UdyogAadharRequest;
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
public class UdyogAadharService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${zoop.udyog.url}")
    private String zoopUrl;

    @Value("${zoop.app.id}")
    private String appId;

    @Value("${zoop.api.key}")
    private String apiKeyZoop;

    public UdyogAadharService(WebClient webClient,
                              TbClientMasterRepository clientRepo,
                              TbUsageHistoryRepository usageRepo,
                              TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyUdyogAadhar(UdyogAadharRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = zoopUrl; // e.g. https://live.zoop.one/api/v1/in/merchant/udyog/lite

        Map<String, Object> payload = new HashMap<>();
        payload.put("mode", "sync");
        payload.put("task_id", UUID.randomUUID().toString());
        Map<String, Object> data = new HashMap<>();
        data.put("udyog_aadhaar", request.getAADHAR_NO());
        data.put("consent", "Y");
        data.put("consent_text", "I hear by declare my consent agreement for fetching my information via ZOOP API.");
        payload.put("data", data);

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> formattedResponse = new HashMap<>();

        int status = 1;
        String message = "Success";

        try {
            apiResponse = webClient.post()
                    .uri(url)
                    .header("app-id", appId)
                    .header("api-key", apiKeyZoop)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            formattedResponse = formatResponse(apiResponse);

        } catch (WebClientResponseException e) {
            status = 0;
            message = "Zoop API Error: " + e.getStatusCode().value();

            apiResponse.put("status", e.getStatusCode().value());
            apiResponse.put("error", e.getResponseBodyAsString());

            Map<String, Object> errResult = Map.of("error", e.getResponseBodyAsString());
            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "data", Map.of("result", errResult)
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();

            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            Map<String, Object> errResult = Map.of("error", e.getMessage());
            formattedResponse = Map.of(
                    "code", 500,
                    "data", Map.of("result", errResult)
            );
        } finally {
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

                // Change ID if your API Type differs
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
        Map<String, Object> result = (Map<String, Object>) apiResponse.get("result");
        if (result == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("code", 500);
            out.put("message", "No data returned from Zoop API");
            out.put("data", error);
            return out;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nic_code", result.getOrDefault("nic_code", new ArrayList<>()));
        data.put("location_of_plant_details", result.getOrDefault("plant_address", new ArrayList<>()));
        data.put("name_of_enterprise", result.getOrDefault("name_of_enterprise", ""));
        data.put("major_activity", result.getOrDefault("major_activity", ""));
        data.put("social_category", result.getOrDefault("social_category", ""));
        data.put("date_of_commencement", result.getOrDefault("date_of_commencement", "NA"));
        data.put("dic_name", result.getOrDefault("dic_name", ""));
        data.put("state", result.getOrDefault("state", "NA"));
        data.put("applied_date", result.getOrDefault("applied_date", "NA"));
        data.put("flat", result.getOrDefault("flat", ""));
        data.put("name_of_building", result.getOrDefault("name_of_building", ""));
        data.put("road", result.getOrDefault("road", ""));
        data.put("village", result.getOrDefault("village", ""));
        data.put("city", result.getOrDefault("city", ""));
        data.put("pin", result.getOrDefault("pin", "NA"));
        data.put("mobile_number", result.getOrDefault("mobile_number", ""));
        data.put("email", result.getOrDefault("email", ""));
        data.put("organization_type", result.getOrDefault("organization_type", ""));
        data.put("gender", result.getOrDefault("gender", ""));
        data.put("date_of_incorporation", result.getOrDefault("date_of_incorporation", "NA"));
        data.put("msme_dfo", result.getOrDefault("msme_dfo", ""));
        data.put("registration_date", result.getOrDefault("registration_date", "NA"));
        data.put("enterprise_type_list", result.getOrDefault("enterprise_type", "NA"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", 200);
        out.put("message", "Success");
        out.put("data", data);
        return out;
    }
}
