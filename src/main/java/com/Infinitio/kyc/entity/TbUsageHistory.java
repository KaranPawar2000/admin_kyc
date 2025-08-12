package com.Infinitio.kyc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usage_history")
@Data
public class TbUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime sentTime;

    @ManyToOne
    @JoinColumn(name = "api_type_id")
    private TbApiTypeMaster apiType;

    private String apiRequestBody;
    @Column(columnDefinition = "TEXT")
    private String apiResponseBody;

    private Integer status;
    private String message;
    private String messageCode;

    @Column(columnDefinition = "TEXT")
    private String data;

    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private TbClientMaster client;

    private String vendorRequestBody;
}

