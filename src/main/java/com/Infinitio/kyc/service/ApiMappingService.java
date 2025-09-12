package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.ApiTypeRouteDTO;
import com.Infinitio.kyc.entity.TbApiTypeRouteMapping;
import com.Infinitio.kyc.repository.TbApiTypeRouteMappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiMappingService {

    private final TbApiTypeRouteMappingRepository mappingRepository;

    public ApiMappingService(TbApiTypeRouteMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public List<ApiTypeRouteDTO> getMappingsByClientUserId(Integer clientUserId) {
        List<TbApiTypeRouteMapping> mappings = mappingRepository.findByClientUserId(clientUserId);

        return mappings.stream().map(m -> new ApiTypeRouteDTO(
                m.getApiType().getId(),
                m.getApiType().getName(),
                m.getApiRoute() != null ? m.getApiRoute().getId() : null,
                m.getApiRoute() != null ? m.getApiRoute().getName() : null
        )).collect(Collectors.toList());
    }
}
