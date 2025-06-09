package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRequestRepository {

    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long currentId = 1L;

    public ItemRequest save(ItemRequest request) {
        if (request.getId() == null) {
            request.setId(currentId++);
        }
        requests.put(request.getId(), request);
        return request;
    }

    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests.values());
    }

    public List<ItemRequest> findByRequestorId(Long userId) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(userId))
                .collect(Collectors.toList());
    }
}

