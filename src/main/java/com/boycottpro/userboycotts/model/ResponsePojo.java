package com.boycottpro.userboycotts.model;

import java.util.List;

public class ResponsePojo {

    private boolean isBoycotting;
    private String company_id;
    private String company_name;
    private String boycottingSince;
    private List<CauseSummary> reasons;

    public ResponsePojo() {
    }

    public ResponsePojo(boolean isBoycotting, String company_id, String company_name,
                        String boycottingSince, List<CauseSummary> reasons) {
        this.isBoycotting = isBoycotting;
        this.company_id = company_id;
        this.company_name = company_name;
        this.boycottingSince = boycottingSince;
        this.reasons = reasons;
    }

    public boolean isBoycotting() {
        return isBoycotting;
    }

    public void setBoycotting(boolean boycotting) {
        isBoycotting = boycotting;
    }

    public String getBoycottingSince() {
        return boycottingSince;
    }

    public void setBoycottingSince(String boycottingSince) {
        this.boycottingSince = boycottingSince;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public List<CauseSummary> getReasons() {
        return reasons;
    }

    public void setReasons(List<CauseSummary> reasons) {
        this.reasons = reasons;
    }
}
