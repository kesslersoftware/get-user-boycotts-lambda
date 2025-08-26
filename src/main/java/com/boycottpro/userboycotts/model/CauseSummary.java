package com.boycottpro.userboycotts.model;

public class CauseSummary {
    private String cause_id;
    private String cause_desc;
    private boolean personal_reason;

    public CauseSummary() {
    }

    public CauseSummary(String cause_id, String cause_desc, boolean personal_reason) {
        this.cause_id = cause_id;
        this.cause_desc = cause_desc;
        this.personal_reason = personal_reason;
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

    public boolean isPersonal_reason() {
        return personal_reason;
    }

    public void setPersonal_reason(boolean personal_reason) {
        this.personal_reason = personal_reason;
    }
}
