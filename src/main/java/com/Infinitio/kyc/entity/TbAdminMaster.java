package com.Infinitio.kyc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_admin_master")
@Data
public class TbAdminMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String emailId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // FK to tb_client_master(id)
    private TbClientMaster client;


    private Byte isActive;
    private String password;
    private String mobileNo;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private TbRoleMaster role;

    private Integer clientId;
}
