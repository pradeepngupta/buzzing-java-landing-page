package com.buzzingjava.waitlist;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test & !export")
public class GoogleSheetsService {
    private final Sheets sheets;
    private final String spreadsheetId;
    private final String range;

    public GoogleSheetsService(
            Sheets sheets,
            @Value("${google.sheets.spreadsheet-id:}") String spreadsheetId,
            @Value("${google.sheets.range:Sheet1!A:E}") String range) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
        this.range = range;
    }

    public void append(WaitlistSheetRow row) {
        if (spreadsheetId.isBlank()) {
            throw new IllegalStateException(
                    "Google Sheets is not configured. Set GOOGLE_SHEET_ID to the target spreadsheet ID.");
        }

        List<Object> values = List.of(
            row.name(), row.email(), row.party(), row.expectations(), row.otherExpectation());
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

    public int getRowCount() {
        if (spreadsheetId.isBlank()) {
            throw new IllegalStateException(
                    "Google Sheets is not configured. Set GOOGLE_SHEET_ID to the target spreadsheet ID.");
        }

        try {
            ValueRange response = sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues() == null ? 0 : response.getValues().size();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the waitlist count from Google Sheets.", exception);
        }
    }
}