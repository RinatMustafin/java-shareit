package ru.practicum.shareit.exception;

public class NotFoundExceptionRequest extends RuntimeException {
    public NotFoundExceptionRequest(String message) {
        super(message);
    }
}
