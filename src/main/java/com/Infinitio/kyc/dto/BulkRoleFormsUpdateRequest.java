package com.Infinitio.kyc.dto;

import java.util.List;

public class BulkRoleFormsUpdateRequest {
    private Integer roleId;
    private List<RoleFormUpdateDTO> formUpdates;

    // Getters and Setters
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public List<RoleFormUpdateDTO> getFormUpdates() {
        return formUpdates;
    }

    public void setFormUpdates(List<RoleFormUpdateDTO> formUpdates) {
        this.formUpdates = formUpdates;
    }
}