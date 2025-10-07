package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.PassportRequest;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PassportService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public PassportService(WebClient webClient,
                           TbClientMasterRepository clientRepo,
                           TbUsageHistoryRepository usageRepo,
                           TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyPassport(PassportRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/passport/passport/passport-details";

        Map<String, Object> payload = new HashMap<>();
        payload.put("id_number", request.getIdNumber());
        payload.put("dob", request.getDob());

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
            // Surepass returned an HTTP error (4xx/5xx)
            status = 0;
            message = "Surepass API Error: " + e.getStatusCode().value();

            apiResponse.put("status", e.getStatusCode().value());
            apiResponse.put("error", e.getResponseBodyAsString());

            // Keep structure similar to other error handling — return code + data.result with error
            Map<String, Object> errResult = Map.of("error", e.getResponseBodyAsString());
            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "data", Map.of("result", errResult)
            );

        } catch (Exception e) {
            // Any other internal failure
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
            // Always save usage log (success or error)
            try {
                TbUsageHistory log = new TbUsageHistory();
                log.setApiRequestBody(mapper.writeValueAsString(request));
                log.setVendorRequestBody(mapper.writeValueAsString(payload));
                log.setApiResponseBody(mapper.writeValueAsString(apiResponse));
                log.setData(mapper.writeValueAsString(formattedResponse.get("data")));
                log.setStatus(status); // 1 success, 0 failure
                log.setMessage(message);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                // CHANGE THE ID (8) IF YOUR DB USES DIFFERENT API TYPE ID FOR PASSPORT
                TbApiTypeMaster apiType = apiTypeRepo.findById(8)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 8 not found"));
                log.setApiType(apiType);

                usageRepo.save(log);
            } catch (JsonProcessingException e) {
                // logging serialization failure
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
            // return empty result if vendor didn't provide data
            Map<String, Object> resultEmpty = new LinkedHashMap<>();
            Map<String, Object> finalData = new LinkedHashMap<>();
            finalData.put("result", resultEmpty);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("code", 500);
            out.put("data", finalData);
            return out;
        }

        // helpers
        java.util.function.Function<Object, String> toStr = v -> v == null ? "" : v.toString();
        DateTimeFormatter isoFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // full name split into givenName and surname
        String fullName = toStr.apply(data.get("full_name")).trim();
        String givenName = "";
        String surname = "";
        if (!fullName.isEmpty()) {
            String[] parts = fullName.split("\\s+");
            if (parts.length == 1) {
                givenName = parts[0];
                surname = "";
            } else {
                surname = parts[parts.length - 1];
                givenName = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
            }
        }

        String fileNumber = toStr.apply(data.get("file_number"));
        String dobIso = toStr.apply(data.get("dob"));
        String dobFormatted = formatDateString(dobIso, outFmt);
        String doaIso = toStr.apply(data.get("date_of_application"));
        String doaFormatted = formatDateString(doaIso, outFmt);
        String applicationType = toStr.apply(data.get("application_type")).toUpperCase();

        // Create ordered result map exactly with the sequence you requested
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileNumber", fileNumber);
        result.put("givenName", givenName);
        result.put("surname", surname);
        result.put("typeOfApplication", applicationType);
        result.put("applicationReceivedOnDate", doaFormatted);
        result.put("name", fullName);
        result.put("dob", dobFormatted);

        Map<String, Object> finalData = new LinkedHashMap<>();
        finalData.put("result", result);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", 200);
        out.put("data", finalData);

        return out;
    }

    private String formatDateString(String isoDate, DateTimeFormatter outFmt) {
        if (isoDate == null || isoDate.isBlank()) return "";
        try {
            LocalDate d = LocalDate.parse(isoDate); // expects yyyy-MM-dd
            return d.format(outFmt);
        } catch (DateTimeParseException e) {
            // if it's already in some other format, try to return original or empty
            return isoDate;
        }
    }
}
