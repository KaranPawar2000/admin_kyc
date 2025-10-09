package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.CorporateFssaiRequest;
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
public class CorporateFssaiService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public CorporateFssaiService(WebClient webClient,
                                 TbClientMasterRepository clientRepo,
                                 TbUsageHistoryRepository usageRepo,
                                 TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyCorporateFssai(CorporateFssaiRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/corporate/fssai";

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

                // Change ID if required (e.g. 11 for Corporate FSSAI)
                TbApiTypeMaster apiType = apiTypeRepo.findById(16)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 16 not found"));
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
                    "message", "Empty response from Surepass",
                    "data", Map.of()
            );
        }

        java.util.function.Function<Object, String> toStr = v -> v == null ? "" : v.toString();

        String fssaiNumber = toStr.apply(data.get("fssai_number"));
        List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");
        Map<String, Object> detail = (details != null && !details.isEmpty()) ? details.get(0) : Collections.emptyMap();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gstin", ""); // As requested: empty
        result.put("fssai_number", fssaiNumber);
        result.put("address", toStr.apply(detail.get("address")));
        result.put("fbo_id", detail.get("fbo_id"));
        result.put("display_ref_id", toStr.apply(detail.get("display_ref_id")));
        result.put("license_category_name", toStr.apply(detail.get("license_category_name")));
        result.put("state_name", toStr.apply(detail.get("state_name")));
        result.put("status_desc", toStr.apply(detail.get("status_desc")));
        result.put("company_name", toStr.apply(detail.get("company_name")));
        result.put("license_active_flag", detail.get("license_active_flag"));
        result.put("ref_id", detail.get("ref_id"));
        result.put("app_type_desc", toStr.apply(detail.get("app_type_desc")));
        result.put("premise_pincode", detail.get("premise_pincode"));

        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("code", toStr.apply(apiResponse.getOrDefault("status_code", 200)));
        formatted.put("message", toStr.apply(apiResponse.getOrDefault("message", "Success")));
        formatted.put("data", result);

        return formatted;
    }
}
