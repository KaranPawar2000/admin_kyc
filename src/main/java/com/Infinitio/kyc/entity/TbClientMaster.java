package com.Infinitio.kyc.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_client_master")
@Data
public class TbClientMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String address;
    private String pincode;
    private String emailId;
    private String mobileNo;
    private String password;
    private String apiKey;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = true)
    private TbRoleMaster role;

    private Byte status;
    private String orgName;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;
    private Integer clientId;
    private LocalDate expiryDate;
    private Integer clientCount;
    private String logo;
    private Byte isEncrypted;
    private String clientKey;

    // ✅ One client can have many admins
    @OneToMany(mappedBy = "client")
    private java.util.List<TbAdminMaster> admins;
}