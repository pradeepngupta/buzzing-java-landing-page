package com.buzzingjava.waitlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.List;
import org.junit.jupiter.api.Test;

class GoogleSheetsServiceTests {
    @Test
    void derivesEmailColumnFromHeader() {
        List<List<Object>> headers = List.of(
                List.<Object>of("Timestamp", "Name", "  eMaIl  ", "Party"));

        assertThat(GoogleSheetsService.findEmailColumnRange("Waitlist!A:L", headers))
                .isEqualTo("Waitlist!C:C");
    }

    @Test
    void derivesEmailColumnBeyondColumnZ() {
        List<Object> headers = new java.util.ArrayList<>();
        for (int index = 0; index < 27; index++) {
            headers.add("Column " + index);
        }
        headers.add("Email");

        assertThat(GoogleSheetsService.findEmailColumnRange("Waitlist!A:AB", List.of(headers)))
                .isEqualTo("Waitlist!AB:AB");
    }

    @Test
    void failsWhenEmailHeaderIsMissing() {
        List<List<Object>> headers = List.of(List.<Object>of("Timestamp", "Name"));

        assertThatIllegalStateException()
                .isThrownBy(() -> GoogleSheetsService.findEmailColumnRange("Waitlist!A:L", headers))
                .withMessageContaining("does not contain an 'Email' column");
    }
}
