package com.Infinitio.kyc.service;
import com.Infinitio.kyc.dto.DrivingLicenseRequest;
import com.Infinitio.kyc.entity.TbApiTypeMaster;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.repository.TbApiTypeMasterRepository;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class Signzy_DrivingLicenseService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;



    public Signzy_DrivingLicenseService(WebClient webClient,
                                 TbClientMasterRepository clientRepo,
                                 TbUsageHistoryRepository usageRepo,
                                 TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyDrivingLicense(DrivingLicenseRequest request, String apiKeyHeader) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> formattedResponse = new LinkedHashMap<>();
        Map<String, Object> apiResponse = new LinkedHashMap<>();

        int status = 1;
        String message = "Success";
        String message_code = "sucess";
        TbClientMaster client = clientRepo.findByApiKey(apiKeyHeader);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKeyHeader);

        String url = "https://api.signzy.app/api/v3/dl_number/based_search";

        // ✅ Convert DOB from YYYY-MM-DD to DD/MM/YYYY
        String formattedDob = null;
        try {
            LocalDate date = LocalDate.parse(request.getDob(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            formattedDob = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd, got: " + request.getDob());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("number", request.getIdNumber());
        payload.put("dob", formattedDob); // ✅ use the converted format

        System.out.println("Payload: " + payload);
        try {
            apiResponse = webClient.post()
                    .uri(url)
                    .header("Authorization", "SwpIcoivb1Brr6xsmbK4vOYgpKMe1axT")
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            formattedResponse = formatResponse(apiResponse);

        } catch (WebClientResponseException e) {
            status = 0;
            message = "Signzy API Error: " + e.getStatusCode().value();
            message_code = "sucess";
            Map<String, Object> errResult = Map.of("error", e.getResponseBodyAsString());
            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "message", message,
                    "data", Map.of("result", errResult)
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();
            message_code = "sucess";
            Map<String, Object> errResult = Map.of("error", e.getMessage());
            formattedResponse = Map.of(
                    "code", 500,
                    "message", message,
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
                log.setMessageCode(message_code);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                TbApiTypeMaster apiType = apiTypeRepo.findById(9) // <-- Change this ID for DL
                        .orElseThrow(() -> new RuntimeException("API Type with ID 9 not found"));
                log.setApiType(apiType);

                usageRepo.save(log);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing usage log", e);
            }
        }

        return formattedResponse;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> formatResponse(Map<String, Object> apiResponse) {
        if (apiResponse == null || apiResponse.isEmpty()) {
            return Map.of(
                    "code", 500,
                    "message", "Empty response from Signzy",
                    "data", Map.of("result", Map.of())
            );
        }

        // wrap the API result inside your desired structure
        Object result = apiResponse.get("result");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("result", result);

        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("code", 200);
        formatted.put("message", "Success");
        formatted.put("data", data);

        return formatted;
    }
}
