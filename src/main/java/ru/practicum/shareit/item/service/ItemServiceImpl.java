package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.exception.NotFoundException;
import ru.practicum.shareit.user.exception.NotFoundOwnerException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElse(null);
        }
        Item item = ItemMapper.toItem(itemDto, owner, request);
        item = itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundOwnerException("Редактировать может только владелец");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        item.setAvailable(itemDto.getAvailable()); // можно обновлять статус
        item = itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lower = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> i.getName().toLowerCase().contains(lower) ||
                        i.getDescription().toLowerCase().contains(lower))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}

