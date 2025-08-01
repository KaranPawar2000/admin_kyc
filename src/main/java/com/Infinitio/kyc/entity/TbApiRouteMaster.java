package com.Infinitio.kyc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_route_master")
public class TbApiRouteMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private TbClientMaster client;

    private LocalDateTime createdModifiedDatetime;
}

