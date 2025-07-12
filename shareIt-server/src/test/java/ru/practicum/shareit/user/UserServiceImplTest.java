package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUser_whenValid_thenUpdated() {
        long userId = 1L;
        User user = new User(userId, "Old Name", "old@example.com");
        UserDto updateDto = new UserDto(userId, "New Name", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        UserDto result = userService.update(userId, updateDto);


        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenEmailUnique_thenUserCreated() {
        UserDto userDto = new UserDto(null, "John", "john@example.com");
        User savedUser = new User(1L, "John", "john@example.com");

        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.create(userDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenEmailAlreadyExists_thenThrowConflictException() {
        UserDto userDto = new UserDto(null, "John", "john@example.com");

        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(userDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_whenEmailAlreadyExists_thenThrowConflictException() {
        long userId = 1L;
        UserDto updateDto = new UserDto(userId, "Updated Name", "taken@example.com");
        User existingUser = new User(userId, "Current Name", "current@example.com");

        when(userRepository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(new User(2L, "Other", "taken@example.com")));

        assertThrows(ConflictException.class, () -> userService.update(userId, updateDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_whenUserExists_thenReturnUserDto() {
        long userId = 1L;
        User user = new User(userId, "Test Name", "test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test Name");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(userId);
    }

    @Test
    void getUser_whenUserNotFound_thenThrowException() {
        long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getById(userId));
    }

    @Test
    void getById_whenUserExists_thenReturnUserDto() {
        long userId = 1L;
        User user = new User(userId, "Test Name", "test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test Name");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(userId);
    }

    @Test
    void getById_whenUserNotFound_thenThrowNotFoundException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void getAll_whenUsersExist_thenReturnUserDtoList() {
        List<User> users = List.of(
                new User(1L, "User One", "one@example.com"),
                new User(2L, "User Two", "two@example.com")
        );

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(result).extracting(UserDto::getEmail).contains("one@example.com", "two@example.com");

        verify(userRepository).findAll();
    }

    @Test
    void deleteById_whenUserExists_thenDeleted() {
        long userId = 1L;
        User user = new User(userId, "Test", "test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }


    @Test
    void deleteById_whenUserNotFound_thenThrowNotFoundException() {
        long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(userId));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getById_whenUserExists_thenReturnUser() {
        long userId = 1L;
        User user = new User(userId, "Test User", "test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(userId);
    }

    @Test
    void getAll_whenUsersExist_thenReturnListOfUserDtos() {
        List<User> users = List.of(
                new User(1L, "User One", "one@example.com"),
                new User(2L, "User Two", "two@example.com")
        );

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("User One", "User Two");

        verify(userRepository).findAll();
    }

    @Test
    void delete_whenUserExists_thenUserDeleted() {
        long userId = 1L;
        User existingUser = new User(userId, "User", "user@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_whenUserNotFound_thenThrowNotFoundException() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(userId));

        verify(userRepository, never()).deleteById(anyLong());
    }
}