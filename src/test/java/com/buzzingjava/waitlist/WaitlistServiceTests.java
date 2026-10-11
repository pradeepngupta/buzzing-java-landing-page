package com.buzzingjava.waitlist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WaitlistServiceTests {
    @Test
    void loadsEmailsAtStartupAndUsesSetToRejectDuplicatesWithoutAnotherRead() {
        FakeGoogleSheetsService sheets = new FakeGoogleSheetsService(List.of("Email", "person@example.com"), false);
        WaitlistService service = new WaitlistService(null, Optional.of(sheets));

        service.loadAcceptedEmails();

        assertThat(service.join(request("PERSON@example.com")).message()).isEqualTo("You are on the list!");
        assertThat(service.currentCount().count()).isEqualTo(1);
        assertThat(sheets.emailReadCount).isEqualTo(1);
        assertThat(sheets.appendCount).isZero();
    }

    @Test
    void addsEmailOnceAndAllowsRetryAfterAppendFailure() {
        FakeGoogleSheetsService sheets = new FakeGoogleSheetsService(List.of("Email"), true);
        WaitlistService service = new WaitlistService(null, Optional.of(sheets));
        service.loadAcceptedEmails();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.join(request("person@example.com")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("temporary append failure");
        service.join(request("PERSON@example.com"));

        assertThat(sheets.appendCount).isEqualTo(2);
        assertThat(sheets.emailReadCount).isEqualTo(1);
        assertThat(service.currentCount().count()).isEqualTo(1);
    }

    private static final class FakeGoogleSheetsService extends GoogleSheetsService {
        private final List<String> existingEmails;
        private boolean failFirstAppend;
        private int emailReadCount;
        private int appendCount;

        FakeGoogleSheetsService(List<String> existingEmails, boolean failFirstAppend) {
            super(null, "spreadsheet-id", "Waitlist!A:L");
            this.existingEmails = existingEmails;
            this.failFirstAppend = failFirstAppend;
        }

        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public List<String> getEmails() {
            emailReadCount++;
            return existingEmails;
        }

        @Override
        public void append(WaitlistSheetRow row) {
            appendCount++;
            if (failFirstAppend) {
                failFirstAppend = false;
                throw new IllegalStateException("temporary append failure");
            }
        }
    }

    private static WaitlistService.WaitlistRequest request(String email) {
        return new WaitlistService.WaitlistRequest(
                "Person", email, "Online", List.of(), "", null, null, null, null, "127.0.0.1", true);
    }
}
