package com.buzzingjava.waitlist;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test & !export")
public class GoogleSheetsService {
    private final Sheets sheets;
    private final String spreadsheetId;
    private final String range;
    private final String emailRange;

    public GoogleSheetsService(
            Sheets sheets,
            @Value("${google.sheets.spreadsheet-id:}") String spreadsheetId,
            @Value("${google.sheets.range:Waitlist!A:E}") String range,
            @Value("${google.sheets.email-range:Waitlist!B:B}") String emailRange) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
        this.range = range;
        this.emailRange = emailRange;
    }

    public void append(WaitlistSheetRow row) {
        if (spreadsheetId.isBlank()) {
            throw new IllegalStateException(
                    "Google Sheets is not configured. Set GOOGLE_SHEET_ID to the target spreadsheet ID.");
        }

        List<Object> values = buildRow(row);
        ValueRange body = new ValueRange().setValues(List.of(values));
        try {
            sheets.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to append the waitlist entry to Google Sheets.", exception);
        }
    }

    public static List<Object> buildRow(WaitlistSheetRow row) {
        // WARNING: this list order must always match the Google Sheet column order exactly.
        // Omitting any element, even when a value is "missing", silently shifts every subsequent column left.
        List<Object> values = new java.util.ArrayList<>(11);
        values.add(normalize(row.timestamp()));
        values.add(normalize(row.name()));
        values.add(normalize(row.email()));
        values.add(normalize(row.partyInterest()));
        values.add(normalize(row.utmSource()));
        values.add(normalize(row.utmMedium()));
        values.add(normalize(row.utmCampaign()));
        values.add(normalize(row.ip()));
        values.add(normalize(row.country()));
        values.add(normalize(row.countryCode()));
        values.add(normalize(row.expectations()));
        return values;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    public List<String> getEmails() {
        if (spreadsheetId.isBlank()) {
            throw new IllegalStateException(
                    "Google Sheets is not configured. Set GOOGLE_SHEET_ID to the target spreadsheet ID.");
        }

        try {
            ValueRange response = sheets.spreadsheets().values()
                    .get(spreadsheetId, emailRange)
                    .execute();
            if (response.getValues() == null) {
                return List.of();
            }
            return response.getValues().stream()
                    .map(row -> row.isEmpty() ? "" : Objects.toString(row.get(0), ""))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read waitlist emails from Google Sheets.", exception);
        }
    }
}