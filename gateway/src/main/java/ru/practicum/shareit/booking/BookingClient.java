package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.rootUri(serverUrl).build());
    }

    public ResponseEntity<Object> create(Long userId, BookingDto bookingDto) {
        return post(API_PREFIX, userId, bookingDto);
    }

    public ResponseEntity<Object> update(Long userId, Long bookingId, boolean approved) {
        String path = String.format("%s/%d?approved=%s", API_PREFIX, bookingId, approved);
        return patch(path, userId, null);
    }

    public ResponseEntity<Object> getById(Long bookingId, Long userId) {
        return get(API_PREFIX + "/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByUser(Long userId, String state, int from, int size) {
        Map<String, Object> params = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get(API_PREFIX + "?state={state}&from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, String state, int from, int size) {
        Map<String, Object> params = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get(API_PREFIX + "/owner?state={state}&from={from}&size={size}", userId, params);
    }
}
