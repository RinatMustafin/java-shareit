package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Component
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";
    private static final String USER_HEADER = "X-Sharer-User-Id";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.rootUri(serverUrl).build());
    }

    public ResponseEntity<Object> create(Long userId, ItemDto itemDto) {
        return post(API_PREFIX, userId, itemDto);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, ItemDto itemDto) {
        return patch(API_PREFIX + "/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getById(Long itemId, Long userId) {
        return get(API_PREFIX + "/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId) {
        return get(API_PREFIX, userId);
    }

    public ResponseEntity<Object> search(String text) {
        String path = API_PREFIX + "/search?text=" + text;
        return get(path);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto commentDto) {
        return post(API_PREFIX + "/" + itemId + "/comment", userId, commentDto);
    }
}
