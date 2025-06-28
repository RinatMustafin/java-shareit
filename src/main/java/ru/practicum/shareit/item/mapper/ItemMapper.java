package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                List.of()
        );
    }

    public static ItemDto toDtoWithComments(Item item, List<CommentDto> comments) {
        ItemDto dto = toDto(item);
        dto.setComments(comments);
        return dto;
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    public static ItemWithBookingDto toDtoWithBookingsAndComments(Item item,
                                                                  Booking lastBooking,
                                                                  Booking nextBooking,
                                                                  List<CommentDto> comments) {
        BookingShortDto last = (lastBooking != null) ? toShortDto(lastBooking) : null;
        BookingShortDto next = (nextBooking != null) ? toShortDto(nextBooking) : null;

        return new ItemWithBookingDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                comments,
                last,
                next
        );
    }

    public static BookingShortDto toShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}
