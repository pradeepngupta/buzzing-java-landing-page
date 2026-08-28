package com.buzzingjava.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleSheetsConfig {
    private static final String SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        // GOOGLE_APPLICATION_CREDENTIALS = file path (local dev); GOOGLE_SERVICE_ACCOUNT_KEY_JSON = raw JSON content (Render/production). Only one needs to be set depending on environment.
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        GoogleCredentials credentials;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
                credentials = GoogleCredentials.fromStream(credentialsStream);
            }
        } else {
            String credentialsJson = System.getenv("GOOGLE_SERVICE_ACCOUNT_KEY_JSON");
            if (credentialsJson == null || credentialsJson.isBlank()) {
                throw new IllegalStateException(
                        "Google service account credentials are missing. Set "
                                + "GOOGLE_APPLICATION_CREDENTIALS to a credential file path for local development "
                                + "or GOOGLE_SERVICE_ACCOUNT_KEY_JSON to the raw JSON content for Render/production.");
            }
            credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
        }

        return credentials.createScoped(Collections.singleton(SHEETS_SCOPE));
    }

    @Bean
    public Sheets googleSheets(GoogleCredentials credentials) throws Exception {
        HttpRequestInitializer requestInitializer = new com.google.auth.http.HttpCredentialsAdapter(credentials);
        return new Sheets.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        requestInitializer)
                .setApplicationName("Buzzing Java")
                .build();
    }
}