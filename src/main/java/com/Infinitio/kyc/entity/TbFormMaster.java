package com.Infinitio.kyc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_form_master")
public class TbFormMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Integer parentId;
    private String link;
    private String icon;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private TbClientMaster client;
}
