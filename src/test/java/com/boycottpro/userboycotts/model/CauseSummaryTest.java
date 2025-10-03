package com.boycottpro.userboycotts.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CauseSummaryTest {

    @Test
    void getCause_id() {
        CauseSummary causeSummary = new CauseSummary();
        assertNull(causeSummary.getCause_id());
    }

    @Test
    void setCause_id() {
        CauseSummary causeSummary = new CauseSummary();
        causeSummary.setCause_id(null);
        assertNull(causeSummary.getCause_id());
    }

    @Test
    void getCause_desc() {
        CauseSummary causeSummary = new CauseSummary();
        assertNull(causeSummary.getCause_desc());
    }

    @Test
    void setCause_desc() {
        CauseSummary causeSummary = new CauseSummary();
        causeSummary.setCause_desc(null);
        assertNull(causeSummary.getCause_desc());
    }

    @Test
    void isPersonal_reason() {
    CauseSummary causeSummary = new CauseSummary();
        assertFalse(causeSummary.isPersonal_reason());
    }

    @Test
    void setPersonal_reason() {
    CauseSummary causeSummary = new CauseSummary();
        causeSummary.setPersonal_reason(false);
        assertFalse(causeSummary.isPersonal_reason());
    }
}