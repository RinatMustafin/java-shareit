package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotFoundOwnerException extends RuntimeException {
    public NotFoundOwnerException(String message) {
        super(message);
    }
}
