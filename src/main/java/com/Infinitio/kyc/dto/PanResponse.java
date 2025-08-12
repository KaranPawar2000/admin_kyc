package com.Infinitio.kyc.dto;

public class PanResponse {
    private String code;
    private String message;
    private Data data;

    public PanResponse() {}

    public PanResponse(String code, String message, Data data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String name;
        private String number;
        private String typeOfHolder;
        private String panStatus;
        private String aadhaarSeedingStatus;
        private String lastUpdatedOn;

        public Data() {}

        public Data(String name, String number, String typeOfHolder, String panStatus, String aadhaarSeedingStatus, String lastUpdatedOn) {
            this.name = name;
            this.number = number;
            this.typeOfHolder = typeOfHolder;
            this.panStatus = panStatus;
            this.aadhaarSeedingStatus = aadhaarSeedingStatus;
            this.lastUpdatedOn = lastUpdatedOn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getTypeOfHolder() {
            return typeOfHolder;
        }

        public void setTypeOfHolder(String typeOfHolder) {
            this.typeOfHolder = typeOfHolder;
        }

        public String getPanStatus() {
            return panStatus;
        }

        public void setPanStatus(String panStatus) {
            this.panStatus = panStatus;
        }

        public String getAadhaarSeedingStatus() {
            return aadhaarSeedingStatus;
        }

        public void setAadhaarSeedingStatus(String aadhaarSeedingStatus) {
            this.aadhaarSeedingStatus = aadhaarSeedingStatus;
        }

        public String getLastUpdatedOn() {
            return lastUpdatedOn;
        }

        public void setLastUpdatedOn(String lastUpdatedOn) {
            this.lastUpdatedOn = lastUpdatedOn;
        }
    }
}