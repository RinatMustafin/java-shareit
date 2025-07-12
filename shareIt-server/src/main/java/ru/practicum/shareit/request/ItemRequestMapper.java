package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated()
        );
    }

    public static ItemRequestWithResponsesDto toWithResponsesDto(ItemRequest request, List<Item> items) {
        List<ItemDtoForRequest> itemDtos = items.stream()
                .map(ItemRequestMapper::toItemDtoForRequest)
                .collect(Collectors.toList());

        return new ItemRequestWithResponsesDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemDtos
        );
    }

    public static ItemDtoForRequest toItemDtoForRequest(Item item) {
        return new ItemDtoForRequest(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }
}
