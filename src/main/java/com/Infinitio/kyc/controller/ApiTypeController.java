package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.repository.TbApiTypeMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/types")
public class ApiTypeController {

    @Autowired
    private TbApiTypeMasterRepository apiTypeRepo;

    @GetMapping("/all")
    public List<Map<String, Object>> getAllApiTypes() {
        return apiTypeRepo.findAll().stream().map(apiType -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", apiType.getId());
            map.put("name", apiType.getName());
            return map;
        }).toList();
    }
}

