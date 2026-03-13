package com.prads.chat.infrastructure.adapters.input.rest.exceptions;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        List<FieldError> errors,
        String path
) {
    public record FieldError(String field, String message) {}
}