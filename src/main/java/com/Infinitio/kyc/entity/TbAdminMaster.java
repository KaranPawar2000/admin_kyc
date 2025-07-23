package com.Infinitio.kyc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_admin_master")
public class TbAdminMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String emailId;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private TbRoleMaster role;

    private Byte isActive;
    private String password;
    private String mobileNo;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private TbClientMaster client;

    private Integer userId;
}

