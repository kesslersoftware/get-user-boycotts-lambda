package com.boycottpro.userboycotts.model;

public class CauseSummary {
        private String cause_id;
        private String company_cause_id;
        private String cause_desc;

    public CauseSummary(String cause_id, String company_cause_id, String cause_desc) {
        this.cause_id = cause_id;
        this.company_cause_id = company_cause_id;
        this.cause_desc = cause_desc;
    }

    public CauseSummary() {
    }

    public String getCause_id() {
        return cause_id;
    }

    public void setCause_id(String cause_id) {
        this.cause_id = cause_id;
    }

    public String getCause_desc() {
        return cause_desc;
    }

    public void setCause_desc(String cause_desc) {
        this.cause_desc = cause_desc;
    }

    public String getCompany_cause_id() {
        return company_cause_id;
    }

    public void setCompany_cause_id(String company_cause_id) {
        this.company_cause_id = company_cause_id;
    }
}
