package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    List<ItemRequestWithResponsesDto> getOwnRequests(Long userId);

    List<ItemRequestWithResponsesDto> getAllRequests(Long userId, int from, int size);

    ItemRequestWithResponsesDto getByRequestId(Long userId, Long requestId);
}
