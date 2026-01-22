package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import com.github.theoydr.eventmanagement.enums.BookingFailureReason;
import com.github.theoydr.eventmanagement.exception.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestController
@RequestMapping("/test")
public class TestController {


    public record TestDto(@NotNull(message = MessageKeys.Error.INVALID_FORMAT) String value) {}


    public record NumericDto(Integer number) {}

    public record MissingMessageDto(
            @NotNull(message = "{validation.key.does.not.exist}")
            String value
    ) {}


    @GetMapping("/throw/resource-not-found")
    void throwResourceNotFound() {
        throw new ResourceNotFoundException("User", "id", 1L);
    }

    @GetMapping("/throw/user-exists")
    void throwUserExists() {
        throw new UserAlreadyExistsException("test@email.com");
    }

    @GetMapping("/throw/duplicate-event")
    void throwDuplicateEvent() {
        throw new DuplicateEventException();
    }

    @GetMapping("/throw/operation-not-allowed")
    void throwOperationNotAllowed() {
        throw new OperationNotAllowedException("test");
    }


    @GetMapping("/throw/event-booking-exception")
    void throwEventBookingException() {
        throw new EventBookingException(BookingFailureReason.EVENT_IN_PAST, "test");
    }

    @GetMapping("/throw/runtime-exception")
    void throwRuntimeException() {
        throw new RuntimeException("Something went wrong internally!");
    }

    @PostMapping("/throw/validation")
    void throwValidation(@Valid @RequestBody TestDto dto) {
    }



    @PostMapping("/throw/validation-with-missing-key")
    void throwValidation(@Valid @RequestBody MissingMessageDto dto) {
    }

    @PostMapping("/throw/json-error")
    void throwJsonError(@RequestBody TestDto dto) {
        // Used to test HttpMessageNotReadableException via malformed JSON input
    }

    //Endpoint to trigger DatabindException (Type Mismatch)
    @PostMapping("/throw/type-mismatch")
    void throwTypeMismatch(@RequestBody NumericDto dto) {
    }





}

