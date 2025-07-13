package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = new ItemDto(null, "ItemName", "ItemDescription", true, null, List.of());
        ItemDto returnedItem = new ItemDto(1L, "ItemName", "ItemDescription", true, null, List.of());

        Mockito.when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(returnedItem);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedItem.getId()))
                .andExpect(jsonPath("$.name").value(returnedItem.getName()))
                .andExpect(jsonPath("$.description").value(returnedItem.getDescription()))
                .andExpect(jsonPath("$.available").value(returnedItem.getAvailable()));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        ItemDto itemDto = new ItemDto(null, "UpdatedName", "UpdatedDescription", false, null, List.of());
        ItemDto returnedItem = new ItemDto(1L, "UpdatedName", "UpdatedDescription", false, null, List.of());

        Mockito.when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(returnedItem);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedItem.getId()))
                .andExpect(jsonPath("$.name").value(returnedItem.getName()))
                .andExpect(jsonPath("$.description").value(returnedItem.getDescription()))
                .andExpect(jsonPath("$.available").value(returnedItem.getAvailable()));
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        ItemDto returnedItem = new ItemDto(1L, "ItemName", "ItemDescription", true, null, List.of());

        Mockito.when(itemService.getById(eq(1L), eq(1L))).thenReturn(returnedItem);

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedItem.getId()))
                .andExpect(jsonPath("$.name").value(returnedItem.getName()))
                .andExpect(jsonPath("$.description").value(returnedItem.getDescription()))
                .andExpect(jsonPath("$.available").value(returnedItem.getAvailable()));
    }

    @Test
    void getAllByOwner_ShouldReturnListOfItems() throws Exception {
        ItemDto item1 = new ItemDto(1L, "Item1", "Description1", true, null, List.of());
        ItemDto item2 = new ItemDto(2L, "Item2", "Description2", false, null, List.of());

        Mockito.when(itemService.getAllByOwner(1L)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(item1.getId()))
                .andExpect(jsonPath("$[1].id").value(item2.getId()));
    }

    @Test
    void search_ShouldReturnListOfItems() throws Exception {
        ItemDto item = new ItemDto(1L, "SearchItem", "SomeDescription", true, null, List.of());

        Mockito.when(itemService.search("some")).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "some"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(item.getName()));
    }

    @Test
    void addComment_ShouldReturnComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Nice item!");

        CommentDto returnedComment = new CommentDto();
        returnedComment.setId(1L);
        returnedComment.setText("Nice item!");
        returnedComment.setAuthorName("User1");
        returnedComment.setCreated(LocalDateTime.now());

        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(returnedComment);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedComment.getId()))
                .andExpect(jsonPath("$.text").value(returnedComment.getText()))
                .andExpect(jsonPath("$.authorName").value(returnedComment.getAuthorName()));
    }
}
