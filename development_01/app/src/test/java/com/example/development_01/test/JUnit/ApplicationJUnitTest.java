package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationJUnitTest {

    @Test
    public void testApplicantSorting() {
        List<MockItem> items = new ArrayList<>();
        items.add(new MockItem("ID1", new Timestamp(1000, 0)));
        items.add(new MockItem("ID2", new Timestamp(500, 0)));
        items.add(new MockItem("ID3", new Timestamp(1500, 0)));

        Collections.sort(items, (a, b) -> {
            if (a.timestamp == null || b.timestamp == null) return 0;
            return a.timestamp.compareTo(b.timestamp);
        });

        for (int i = 0; i < items.size(); i++) {
            items.get(i).name = "Applicant " + (i + 1);
        }

        assertEquals("Applicant 1", items.get(0).name);
        assertEquals("ID2", items.get(0).id);
        
        assertEquals("Applicant 2", items.get(1).name);
        assertEquals("ID1", items.get(1).id);
        
        assertEquals("Applicant 3", items.get(2).name);
        assertEquals("ID3", items.get(2).id);
    }

    private static class MockItem {
        String id;
        String name;
        Timestamp timestamp;

        MockItem(String id, Timestamp timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }
    }
}