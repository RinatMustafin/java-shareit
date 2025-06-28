package ru.practicum.shareit.booking.dto;


import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
     Long id;
     LocalDateTime start;
     LocalDateTime end;
     Long itemId;
     Long bookerId;
     Status status;
}
