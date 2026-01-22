package com.github.theoydr.eventmanagement.validator;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventDatesValidatorTest {

    private final EventDatesValidator validator = new EventDatesValidator();

    @Mock
    private ConstraintValidatorContext context;

    @Test
    @DisplayName("Should return true when End date is after Start date")
    void isValid_ValidDates_ReturnsTrue() {
        EventRequest request = createRequest(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        assertThat(validator.isValid(request, context)).isTrue();
    }

    @Test
    @DisplayName("Should return false when End date is before Start date")
    void isValid_EndBeforeStart_ReturnsFalse() {
        EventRequest request = createRequest(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1) // End is before start
        );
        assertThat(validator.isValid(request, context)).isFalse();
    }

    @Test
    @DisplayName("Should return false when End date is equal to Start date")
    void isValid_SameDates_ReturnsFalse() {
        LocalDateTime now = LocalDateTime.now();
        EventRequest request = createRequest(now, now);
        assertThat(validator.isValid(request, context)).isFalse();
    }

    @ParameterizedTest(name = "startDate={0}, endDate={1} should be valid")
    @MethodSource("nullDateProvider")
    @DisplayName("Should return true if dates are null (let @NotNull handle it)")
    void isValid_NullDates_ReturnsTrue(LocalDateTime startDate, LocalDateTime endDate) {
        // We return true because the @NotNull annotation on the fields handles the null check.
        // This validator only cares about the *comparison* if values exist.
        EventRequest request = createRequest(startDate, endDate);
        assertThat(validator.isValid(request, context)).isTrue();
    }

    static Stream<Arguments> nullDateProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(LocalDateTime.now().plusDays(1), null),
                Arguments.of(null, LocalDateTime.now().plusDays(1))
        );
    }

    private EventRequest createRequest(LocalDateTime start, LocalDateTime end) {
        return new EventRequest(
                "Title", "Desc", "Loc",
                start, end,
                100, 10.0, null, 1L
        );
    }
}