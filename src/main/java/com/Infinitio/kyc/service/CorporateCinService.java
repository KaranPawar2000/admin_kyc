package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.CorporateCinRequest;
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
public class CorporateCinService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public CorporateCinService(WebClient webClient,
                               TbClientMasterRepository clientRepo,
                               TbUsageHistoryRepository usageRepo,
                               TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyCIN(CorporateCinRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> formattedResponse = new LinkedHashMap<>();
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> payload = Map.of("id_number", request.getIdNumber());

        int status = 1;
        String message = "Success";

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/corporate/company-details";
//        System.out.println("Payload: " + payload);
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

            Map<String, Object> errResponse;
            try {
                errResponse = mapper.readValue(e.getResponseBodyAsString(), Map.class);
            } catch (Exception ex) {
                errResponse = Map.of("error", e.getResponseBodyAsString());
            }

            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "message", "Surepass API error",
                    "data", Map.of("result", errResponse)
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();

            formattedResponse = Map.of(
                    "code", 500,
                    "message", "Internal Server Error",
                    "data", Map.of("result", Map.of("message", e.getMessage()))
            );
        } finally {
            // Always save usage log
            try {
                System.out.println("Message:- "+ message);
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

                // Update this ID to your CIN API Type ID (replace 9 if needed)
                TbApiTypeMaster apiType = apiTypeRepo.findById(12)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 12 not found"));
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
        if (apiResponse == null || apiResponse.isEmpty() || apiResponse.get("data") == null)
            return Map.of("code", 500, "message", "Empty response", "data", Map.of("result", Collections.emptyMap()));

        Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");
        Map<String, Object> details = (Map<String, Object>) data.get("details");
        Map<String, Object> companyInfo = (Map<String, Object>) details.get("company_info");

        // Extract company info safely
        java.util.function.Function<Object, String> toStr = v -> v == null ? "" : v.toString();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", toStr.apply(data.get("company_name")));
        result.put("number", toStr.apply(data.get("company_id")));
        result.put("company_type", toStr.apply(data.get("company_type")));
        result.put("roc_code", toStr.apply(companyInfo.get("roc_code")));
        result.put("registration_number", toStr.apply(companyInfo.get("registration_number")));
        result.put("company_category", toStr.apply(companyInfo.get("company_category")));
        result.put("class_of_company", toStr.apply(companyInfo.get("class_of_company")));
        result.put("company_subcategory", toStr.apply(companyInfo.get("company_sub_category")));
        result.put("authorised_capital", toStr.apply(companyInfo.get("authorized_capital")));
        result.put("paid_up_capital", toStr.apply(companyInfo.get("paid_up_capital")));
        result.put("date_of_incorporation", toStr.apply(companyInfo.get("date_of_incorporation")));
        result.put("registered_address", toStr.apply(companyInfo.get("registered_address")));
        result.put("address_other_than_ro", toStr.apply(companyInfo.get("address_other_than_ro")));
        result.put("listed_status", toStr.apply(companyInfo.get("listed_status")));
        result.put("active_compliance", toStr.apply(companyInfo.get("active_compliance")));
        result.put("suspended_at_stock_exchange", toStr.apply(companyInfo.get("suspended_at_stock_exchange")));
        result.put("last_agm_date", toStr.apply(companyInfo.get("last_agm_date")));
        result.put("last_bs_date", toStr.apply(companyInfo.get("last_bs_date")));
        result.put("company_status", toStr.apply(companyInfo.get("company_status")));
        result.put("status_under_cirp", toStr.apply(companyInfo.get("status_under_cirp")));

        result.put("directors", details.get("directors"));
        result.put("charges", details.get("charges"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", 200);
        out.put("message", "Success");
        out.put("data", result);

        return out;
    }
}
