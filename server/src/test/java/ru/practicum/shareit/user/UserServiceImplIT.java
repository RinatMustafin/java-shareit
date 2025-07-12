package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
public class UserServiceImplIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_whenEmailNotExists_thenUserCreated() {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        UserDto created = userService.create(userDto);

        assertNotNull(created.getId());
        assertEquals("Test User", created.getName());
        assertEquals("test@example.com", created.getEmail());

        assertTrue(userRepository.findById(created.getId()).isPresent());
    }

    @Test
    void createUser_whenEmailExists_thenThrowConflictException() {

        User user = new User();
        user.setName("Existing");
        user.setEmail("duplicate@example.com");
        userRepository.save(user);


        UserDto userDto = new UserDto();
        userDto.setName("New User");
        userDto.setEmail("duplicate@example.com");

        assertThrows(ConflictException.class, () -> userService.create(userDto));
    }


}
