package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialize() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2025, 7, 9, 12, 0));
        dto.setEnd(LocalDateTime.of(2025, 7, 10, 12, 0));
        dto.setItemId(100L);
        dto.setBookerId(200L);
        dto.setStatus(Status.APPROVED);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"start\":\"2025-07-09T12:00:00\"");
        assertThat(json).contains("\"end\":\"2025-07-10T12:00:00\"");
        assertThat(json).contains("\"itemId\":100");
        assertThat(json).contains("\"bookerId\":200");
        assertThat(json).contains("\"status\":\"APPROVED\"");
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"id\":1,\"start\":\"2025-07-09T12:00:00\",\"end\":\"2025-07-10T12:00:00\"," +
                "\"itemId\":100,\"bookerId\":200,\"status\":\"APPROVED\"}";

        BookingDto dto = objectMapper.readValue(json, BookingDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 7, 9, 12, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 7, 10, 12, 0));
        assertThat(dto.getItemId()).isEqualTo(100L);
        assertThat(dto.getBookerId()).isEqualTo(200L);
        assertThat(dto.getStatus()).isEqualTo(Status.APPROVED);
    }
}
