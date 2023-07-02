package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public UserDto saveUser(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.addUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto user) {
        return UserMapper.toUserDto(userRepository.updateUser(userId, UserMapper.toUser(user)));
    }

    @Override
    public UserDto findUserById(Long id) {
        return UserMapper.toUserDto(userRepository.findUserById(id));
    }

    @Override
    public UserDto deleteUser(Long userId) {
        return UserMapper.toUserDto(userRepository.removeUser(userId));
    }

    @Override
    public Collection<UserDto> findAllUsers() {
        return UserMapper.listToUserDto(userRepository.getAllUsers());
    }

}
