package com.Infinitio.kyc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_role_details")
public class TbRoleDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private TbRoleMaster role;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private TbFormMaster form;

    private Integer seqNo;
    private Byte isAllowed;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private TbClientMaster client;

    private Byte showInMenu;
}

