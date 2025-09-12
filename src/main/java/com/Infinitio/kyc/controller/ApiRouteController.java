package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.RoleDTO;
import com.Infinitio.kyc.entity.RouteDTO;
import com.Infinitio.kyc.entity.TbApiRouteMaster;
import com.Infinitio.kyc.service.RolesService;
import com.Infinitio.kyc.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class ApiRouteController {



    private static final Logger logger = LoggerFactory.getLogger(ApiRouteController.class);

    @Autowired
    private RouteService routeService;

    @GetMapping("/all")
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {
        logger.info("Received request to fetch all API routes");
        return ResponseEntity.ok(routeService.getAllRoutes());
    }


}
