package com.Infinitio.kyc.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_type_master")
@Data
public class TbApiTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private TbClientMaster client;

    private Integer isModify;
    private Byte status;
}

