package com.boycottpro.userboycotts.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponsePojoTest {

    @Test
    void isBoycotting() {
        ResponsePojo responsePojo = new ResponsePojo();
        assertFalse(responsePojo.isBoycotting());
    }

    @Test
    void setBoycotting() {
        ResponsePojo responsePojo = new ResponsePojo();
        responsePojo.setBoycotting(false);
        assertFalse(responsePojo.isBoycotting());
    }

    @Test
    void getBoycottingSince() {
        ResponsePojo responsePojo = new ResponsePojo();
        assertNull(responsePojo.getBoycottingSince());
    }

    @Test
    void setBoycottingSince() {
        ResponsePojo responsePojo = new ResponsePojo();
        responsePojo.setBoycottingSince(null);
        assertNull(responsePojo.getBoycottingSince());
    }

    @Test
    void getCompany_id() {
        ResponsePojo responsePojo = new ResponsePojo();
        assertNull(responsePojo.getCompany_id());
    }

    @Test
    void setCompany_id() {
        ResponsePojo responsePojo = new ResponsePojo();
        responsePojo.setCompany_id(null);
        assertNull(responsePojo.getCompany_name());
    }

    @Test
    void getCompany_name() {
        ResponsePojo responsePojo = new ResponsePojo();
        assertNull(responsePojo.getCompany_name());
    }

    @Test
    void setCompany_name() {
        ResponsePojo responsePojo = new ResponsePojo();
        responsePojo.setCompany_name(null);
        assertNull(responsePojo.getCompany_name());
    }

    @Test
    void getReasons() {
        ResponsePojo responsePojo = new ResponsePojo();
        assertNull(responsePojo.getReasons());
    }

    @Test
    void setReasons() {
        ResponsePojo responsePojo = new ResponsePojo();
        responsePojo.setReasons(null);
        assertNull(responsePojo.getReasons());
    }

    @Test
    void testConstructorWithParameters() {
        ResponsePojo responsePojo = new ResponsePojo(true, "comp123", "Test Company", "2023-10-01T00:00:00Z", null);
        assertTrue(responsePojo.isBoycotting());
        assertEquals("comp123", responsePojo.getCompany_id());
        assertEquals("Test Company", responsePojo.getCompany_name());
        assertEquals("2023-10-01T00:00:00Z", responsePojo.getBoycottingSince());
        assertNull(responsePojo.getReasons());
    }
}