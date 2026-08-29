package com.buzzingjava.waitlist;

public record WaitlistSheetRow(
        String timestamp,
        String name,
        String email,
        String partyInterest,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        String ip,
        String country,
        String countryCode,
        String expectations) {}