package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.Status.APPROVED;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private final LocalDateTime now = LocalDateTime.now();

    private final BookingResponseDto bookingResponse = BookingResponseDto.builder()
            .id(1L)
            .start(now.plusDays(1))
            .end(now.plusDays(2))
            .status(APPROVED)
            .booker(new UserDto(2L, "Booker", "booker@mail.com"))
            .item(new ItemDto(3L, "Drill", "Simple drill", true, null, List.of()))
            .build();

    @Test
    void createBooking_ShouldReturnBookingResponse() throws Exception {
        BookingDto bookingDto = new BookingDto(null, now.plusDays(1), now.plusDays(2), 3L, 2L, null);

        Mockito.when(bookingService.create(eq(1L), any(BookingDto.class))).thenReturn(bookingResponse);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponse.getId()))
                .andExpect(jsonPath("$.status").value(bookingResponse.getStatus().name()))
                .andExpect(jsonPath("$.item.name").value("Drill"))
                .andExpect(jsonPath("$.booker.name").value("Booker"));
    }

    @Test
    void updateBooking_ShouldReturnUpdatedBooking() throws Exception {
        Mockito.when(bookingService.update(1L, 1L, true)).thenReturn(bookingResponse);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_ShouldReturnBooking() throws Exception {
        Mockito.when(bookingService.getById(1L, 1L)).thenReturn(bookingResponse);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.booker.name").value("Booker"));
    }

    @Test
    void getAllByUser_ShouldReturnListOfBookings() throws Exception {
        Mockito.when(bookingService.getAllByUser(1L, "ALL", 0, 10)).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllByOwner_ShouldReturnListOfBookings() throws Exception {
        Mockito.when(bookingService.getAllByOwner(1L, "ALL", 0, 10)).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}