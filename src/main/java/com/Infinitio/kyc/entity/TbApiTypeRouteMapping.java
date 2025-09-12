package com.Infinitio.kyc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_api_type_route_mapping")
public class TbApiTypeRouteMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer clientUserId;

    @ManyToOne
    @JoinColumn(name = "api_type_id")
    private TbApiTypeMaster apiType;

    @ManyToOne
    @JoinColumn(name = "api_route_id")
    private TbApiRouteMaster apiRoute;

    private LocalDateTime createdModifiedDate;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private TbClientMaster client;
}

