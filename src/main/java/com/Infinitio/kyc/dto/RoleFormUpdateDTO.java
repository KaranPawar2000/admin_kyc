package com.Infinitio.kyc.dto;

public class RoleFormUpdateDTO {
    private Integer formId;
    private Integer seqNo;
    private Byte isAllowed;
    private Byte showInMenu;

    // Getters and Setters
    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public Byte getIsAllowed() {
        return isAllowed;
    }

    public void setIsAllowed(Byte isAllowed) {
        this.isAllowed = isAllowed;
    }

    public Byte getShowInMenu() {
        return showInMenu;
    }

    public void setShowInMenu(Byte showInMenu) {
        this.showInMenu = showInMenu;
    }
}