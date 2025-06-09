package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.ConflictException;
import ru.practicum.shareit.user.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()))) {
            throw new ConflictException("Email уже используется: " + userDto.getEmail());
        }
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(user -> UserMapper.toUserDto(user))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        if (userDto.getEmail() != null &&
                userRepository.findAll().stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()))) {
            throw new ConflictException("Email уже используется другим пользователем");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
