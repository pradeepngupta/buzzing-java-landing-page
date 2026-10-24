package com.buzzingjava.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.sheets.v4.Sheets;
import com.buzzingjava.waitlist.GoogleSheetsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class GoogleSheetsConfigTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(GoogleSheetsConfig.class, GoogleSheetsService.class)
            .withPropertyValues(
                    "GOOGLE_APPLICATION_CREDENTIALS=",
                    "GOOGLE_SERVICE_ACCOUNT_KEY_JSON=");

    @Test
    void skipsGoogleSheetsConfigurationWhenCredentialsAreMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(GoogleCredentials.class);
            assertThat(context).doesNotHaveBean(Sheets.class);
            assertThat(context).doesNotHaveBean(GoogleSheetsService.class);
        });
    }
}
