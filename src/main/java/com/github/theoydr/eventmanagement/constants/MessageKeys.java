package com.github.theoydr.eventmanagement.constants;

public final class MessageKeys {

    private MessageKeys() {}

    public static final class UserMessages {
        private UserMessages() {}
        public static final String USERNAME_NOT_BLANK = "{user.validation.username.notBlank}";
        public static final String USERNAME_SIZE = "{user.validation.username.size}";
        public static final String EMAIL_NOT_BLANK = "{user.validation.email.notBlank}";
        public static final String EMAIL_FORMAT = "{user.validation.email.format}";
        public static final String PASSWORD_NOT_BLANK = "{user.validation.password.notBlank}";
        public static final String PASSWORD_SIZE = "{user.validation.password.size}";
        public static final String EMAIL_NOT_FOUND = "{user.exception.emailNotFound}";
        public static final String USER_NOT_FOUND = "{user.exception.userNotFound}";
        public static final String USER_EXISTS_PREFIX = "{user.exception.userExistsPrefix}";
        public static final String USER_EXISTS_SUFFIX = "{user.exception.userExistsSuffix}";


    }

    public static final class EventMessages {
        private EventMessages() {}
        public static final String TITLE_NOT_BLANK = "{event.validation.title.notblank}";
        public static final String TITLE_SIZE = "{event.validation.title.size}";
        public static final String DESCRIPTION_NOT_BLANK = "{event.validation.description.notblank}";
        public static final String DESCRIPTION_SIZE = "{event.validation.description.size}";
        public static final String LOCATION_NOT_BLANK = "{event.validation.location.notblank}";
        public static final String START_DATE_NOT_NULL = "{event.validation.startdate.notnull}";
        public static final String START_DATE_FUTURE = "{event.validation.startdate.future}";
        public static final String END_DATE_NOT_NULL = "{event.validation.enddate.notnull}";
        public static final String END_DATE_FUTURE = "{event.validation.enddate.future}";
        public static final String CAPACITY_NOT_NULL = "{event.validation.capacity.notnull}";
        public static final String CAPACITY_MIN = "{event.validation.capacity.min}";
        public static final String PRICE_NOT_NULL = "{event.validation.price.notnull}";
        public static final String PRICE_MIN = "{event.validation.price.min}";
        public static final String CATEGORY_NOT_NULL = "{event.validation.category.notnull}";
        public static final String ORGANIZER_NOT_NULL = "{event.validation.organizer.notnull}";
        public static final String DATES_VALID = "{event.validation.dates.valid}";
        public static final String EVENT_NOT_FOUND = "{event.exception.eventid.notFound}";
    }

    public static final class BookingMessages {
        private BookingMessages() {}
        public static final String USER_ID_REQUIRED = "{booking.validation.userid.required}";
        public static final String EVENT_ID_REQUIRED = "{booking.validation.eventid.required}";
        public static final String TICKETS_NOT_ZERO = "{booking.validation.tickets.notZero}";
        public static final String TICKETS_MIN = "{booking.validation.tickets.min}";
        public static final String BOOKING_EXISTS = "{booking.exception.bookingExists}";
        public static final String BOOKING_NOT_FOUND = "{booking.exception.notFound}";
        public static final String BOOKING_NOT_FUTURE = "{booking.validation.bookingDate.notFuture}";
    }

    public static final class CommonMessages {
        private CommonMessages() {}
        public static final String REQUIRED = "{common.validation.required}";
        public static final String INVALID_FORMAT = "{common.validation.invalid.format}";
        public static final String VALIDATION_FAILED = "{common.validation.failed}";
    }

    public static final class Error {
        private Error() {}
        public static final String RESOURCE_NOT_FOUND = "{error.resource.notFound}";
        public static final String USER_EXISTS = "{error.user.exists}";
        public static final String EVENT_DUPLICATE = "{error.event.duplicate}";
        public static final String BOOKING_FAILED = "{error.eventmanagement.failed}";
        public static final String MESSAGE_NOT_FOUND = "{error.internal.messageNotFound}";
        public static final String UNEXPECTED_VALIDATION_EXCEPTION = "error.internal.unexpectedValidationException";
        public static final String UNEXPECTED = "error.internal.unexpected";
        public static final String ENDPOINT_NOT_FOUND = "{error.endpoint.notFound}";


    }
}
