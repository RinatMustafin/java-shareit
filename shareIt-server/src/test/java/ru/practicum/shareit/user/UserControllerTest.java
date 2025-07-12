package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        UserDto inputUser = new UserDto(null, "John Doe", "john@example.com");
        UserDto returnedUser = new UserDto(1L, "John Doe", "john@example.com");

        Mockito.when(userService.create(any(UserDto.class))).thenReturn(returnedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.name").value(returnedUser.getName()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserDto returnedUser = new UserDto(1L, "John Doe", "john@example.com");

        Mockito.when(userService.getById(1L)).thenReturn(returnedUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.name").value(returnedUser.getName()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Smith", "jane@example.com");

        Mockito.when(userService.getAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserDto inputUser = new UserDto(null, "John Updated", "john.updated@example.com");
        UserDto returnedUser = new UserDto(1L, "John Updated", "john.updated@example.com");

        Mockito.when(userService.update(eq(1L), any(UserDto.class))).thenReturn(returnedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.name").value(returnedUser.getName()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userService).delete(1L);
    }
}
