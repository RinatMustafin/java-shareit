package ru.practicum.shareit.itemRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_shouldReturnRequest() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Need drill", LocalDateTime.now());

        Mockito.when(requestService.create(anyLong(), any())).thenReturn(dto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(dto.getDescription())));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        ItemRequestWithResponsesDto response = new ItemRequestWithResponsesDto(
                1L, "Need drill", LocalDateTime.now(), Collections.emptyList());

        Mockito.when(requestService.getOwnRequests(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need drill")));
    }

    @Test
    void getAllRequests_shouldReturnPaginatedList() throws Exception {
        ItemRequestWithResponsesDto response = new ItemRequestWithResponsesDto(
                2L, "Need hammer", LocalDateTime.now(), Collections.emptyList());

        Mockito.when(requestService.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].description", is("Need hammer")));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        ItemRequestWithResponsesDto response = new ItemRequestWithResponsesDto(
                3L, "Need laptop", LocalDateTime.now(), Collections.emptyList());

        Mockito.when(requestService.getByRequestId(1L, 3L)).thenReturn(response);

        mockMvc.perform(get("/requests/3")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.description", is("Need laptop")));
    }
}
