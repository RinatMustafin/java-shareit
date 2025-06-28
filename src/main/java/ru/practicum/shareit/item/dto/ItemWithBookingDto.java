package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemWithBookingDto extends ItemDto {
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    public ItemWithBookingDto(Long id, String name, String description, Boolean available, Long requestId,
                              List<CommentDto> comments, BookingShortDto lastBooking, BookingShortDto nextBooking) {
        super(id, name, description, available, requestId, comments);
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
    }
}
