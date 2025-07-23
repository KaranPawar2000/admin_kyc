package com.Infinitio.kyc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_role_master")
public class TbRoleMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Integer parentRoleId;
    private String shortCode;
    private Byte status;
    private Integer sequenceNo;
    private Integer parentId;
    private String parentName;
    private String type;
    private String description;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private TbClientMaster client;

    private String startPage;
}

