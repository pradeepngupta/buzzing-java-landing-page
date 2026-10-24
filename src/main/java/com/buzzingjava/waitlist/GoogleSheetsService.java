package com.buzzingjava.waitlist;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test & !export")
@ConditionalOnBean(Sheets.class)
public class GoogleSheetsService {
    private final Sheets sheets;
    private final String spreadsheetId;
    private final String range;

    public GoogleSheetsService(
            Sheets sheets,
            @Value("${google.sheets.spreadsheet-id:}") String spreadsheetId,
            @Value("${google.sheets.range:Waitlist!A:L}") String range) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
        this.range = range;
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

    public boolean isConfigured() {
        return !spreadsheetId.isBlank();
    }

    public static List<Object> buildRow(WaitlistSheetRow row) {
        // WARNING: this list order must always match the Google Sheet column order exactly.
        // Omitting any element, even when a value is "missing", silently shifts every subsequent column left.
        List<Object> values = new java.util.ArrayList<>(12);
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
        values.add(normalize(row.privacyConsent()));
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
            List<List<Object>> headerValues = sheets.spreadsheets().values()
                    .get(spreadsheetId, headerRange())
                    .execute()
                    .getValues();
            String emailRange = findEmailColumnRange(range, headerValues);
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

    private String headerRange() {
        int separator = range.lastIndexOf('!');
        String sheetQualifier = separator < 0 ? "" : range.substring(0, separator + 1);
        return sheetQualifier + "1:1";
    }

    static String findEmailColumnRange(String configuredRange, List<List<Object>> headerValues) {
        if (headerValues == null || headerValues.isEmpty()) {
            throw new IllegalStateException("Google Sheet header row is empty; unable to find the Email column.");
        }

        List<Object> headers = headerValues.get(0);
        for (int index = 0; index < headers.size(); index++) {
            if ("email".equalsIgnoreCase(Objects.toString(headers.get(index), "").trim())) {
                String column = columnName(index);
                int separator = configuredRange.lastIndexOf('!');
                String sheetQualifier = separator < 0 ? "" : configuredRange.substring(0, separator + 1);
                return sheetQualifier + column + ":" + column;
            }
        }
        throw new IllegalStateException("Google Sheet header row does not contain an 'Email' column.");
    }

    private static String columnName(int zeroBasedIndex) {
        StringBuilder column = new StringBuilder();
        int index = zeroBasedIndex + 1;
        while (index > 0) {
            int remainder = (index - 1) % 26;
            column.append((char) ('A' + remainder));
            index = (index - 1) / 26;
        }
        return column.reverse().toString();
    }
}