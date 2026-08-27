package com.buzzingjava.waitlist;

public record WaitlistSheetRow(
        String name,
        String email,
        String party,
        String expectations,
        String otherExpectation) {}