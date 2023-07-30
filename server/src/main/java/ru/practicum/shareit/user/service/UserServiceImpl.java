package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto user) {
        User newUser = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                String.format("Пользователь с таким id = %d не найден", userId)));
        if (user.getName() != null) {
            newUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            newUser.setEmail(user.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(newUser));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserById(Long userId) {
        User newUser = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                String.format("Пользователь с таким id = %d не найден", userId)));
        return UserMapper.toUserDto(newUser);
    }

    @Override
    public UserDto deleteUser(Long userId) {
        UserDto userDto = findUserById(userId);
        User user = UserMapper.toUser(userDto);
        userRepository.delete(user);
        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> findAllUsers() {
        return UserMapper.listToUserDto(userRepository.findAll());
    }

}