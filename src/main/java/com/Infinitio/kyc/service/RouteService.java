package com.Infinitio.kyc.service;


import com.Infinitio.kyc.entity.RouteDTO;
import com.Infinitio.kyc.entity.TbApiRouteMaster;
import com.Infinitio.kyc.repository.TbApiRouteMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Autowired
    private TbApiRouteMasterRepository routeRepository;

    public List<RouteDTO> getAllRoutes() {
        try {
            logger.info("Fetching all API routes");
            List<TbApiRouteMaster> routes = routeRepository.findAll();

            if (routes.isEmpty()) {
                logger.warn("No API routes found");
                return Collections.emptyList();
            }

            List<RouteDTO> routeDTOs = routes.stream()
                    .map(route -> new RouteDTO(route.getId(), route.getName()))
                    .collect(Collectors.toList());

            logger.info("Found {} API routes", routeDTOs.size());
            return routeDTOs;

        } catch (Exception e) {
            logger.error("Error while fetching API routes: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch API routes", e);
        }
    }


}
