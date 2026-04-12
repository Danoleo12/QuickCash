package com.example.development_01.androidTest;
import com.example.development_01.core.data.PaymentInfo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
public class Paymentinfojunit {

    private PaymentInfo paymentInfo;
    @Before
    public void setUp() {
        paymentInfo = new PaymentInfo("John Doe", "123456789", "021000021", "TD Bank");
    }

    @Test
    public void setsAllFieldsCorrectly() {
        assertEquals("John Doe", paymentInfo.getAccountName());
        assertEquals("123456789", paymentInfo.getAccountNumber());
        assertEquals("021000021", paymentInfo.getRoutingNumber());
        assertEquals("TD Bank", paymentInfo.getBankName());
    }

    @Test
    public void updateFieldsCorrectly() {
        paymentInfo.setAccountName("Jane Smith");
        paymentInfo.setBankName("RBC");
        paymentInfo.setAccountNumber("987654321");
        paymentInfo.setRoutingNumber("011000138");

        assertEquals("Jane Smith", paymentInfo.getAccountName());
        assertEquals("RBC", paymentInfo.getBankName());
        assertEquals("987654321", paymentInfo.getAccountNumber());
        assertEquals("011000138", paymentInfo.getRoutingNumber());
    }

    @Test
    public void createsObjectWithNullFields() {
        PaymentInfo empty = new PaymentInfo();
        assertNotNull(empty);
        assertNull(empty.getAccountName());
        assertNull(empty.getAccountNumber());
        assertNull(empty.getRoutingNumber());
        assertNull(empty.getBankName());
    }
}
