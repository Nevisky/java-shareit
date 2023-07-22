package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    User user;

    @BeforeEach
    void addUser() {
        user = new User();
        user.setId(1L);
        user.setName("Test");
        user.setEmail("Test@yandex.ru");
    }

    @Test
    void saveUser_whenUserIsValid_thenSavedUser() {

        when(userRepository.save(any())).thenReturn(user);
        UserDto userDto = UserMapper.toUserDto(user);
        UserDto actualUser = userService.saveUser(userDto);

        assertNotNull(actualUser);
        assertEquals(user.getId(), actualUser.getId());
        verify(userRepository, times(1))
                .save(any());

    }

    @Test
    void saveUser_whenUserEmailIsDuplicated_thenThrowException() {
        when(userRepository.save(any())).thenThrow(ValidationException.class);
        UserDto result = UserMapper.toUserDto(user);

        assertThrows(ValidationException.class,
                () -> userService.saveUser(result));

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateNameUser_whenNameUserIsValid_thenUpdateNameUser() {
        User updateUser = new User();
        updateUser.setId(user.getId());
        String nameUpdated = "Update";
        updateUser.setName(nameUpdated);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(updateUser);
        UserDto mapperUpdateUser = UserMapper.toUserDto(updateUser);
        UserDto result = userService.updateUser(mapperUpdateUser.getId(), UserDto.builder().name("Update").build());
        assertEquals(nameUpdated, result.getName());
    }

    @Test
    void updateEmailUser_whenEmailUserIsValid_thenUpdateEmailUser() {
        User updateUser = new User();
        updateUser.setId(user.getId());
        String emailUpdated = "update@yandex.ru";
        updateUser.setEmail(emailUpdated);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(updateUser);
        UserDto mapperUpdateUser = UserMapper.toUserDto(updateUser);
        UserDto result = userService.updateUser(mapperUpdateUser.getId(), UserDto.builder().email("update@yandex.ru").build());
        assertEquals(emailUpdated, result.getEmail());
    }

    @Test
    void updateUser_whenUserNotFound_thenReturnObjectNotFoundException() {
        long userId = 999L;
        UserDto updateUser = UserMapper.toUserDto(user);
        String error = String.format("Пользователь с таким id = %d не найден", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.updateUser(userId, updateUser));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void getAllUsers_whenListFilled_thenReturnListUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        Collection<UserDto> users = userService.findAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void getAllUsers_whenListIsEmpty_thenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        Collection<UserDto> users = userService.findAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void getUserById_whenUserIdIsValid_thenReturnUser() {
        long userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto user = userService.findUserById(userId);

        assertNotNull(user);
        assertEquals(userId, user.getId());
    }

    @Test
    void getUserById_WhenUserNotFound_thenReturnObjectNotFoundException() {
        long userId = 999L;
        String error = String.format("Пользователь с таким id = %d не найден", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.findUserById(userId));

        assertEquals(error, exception.getMessage());
    }

    @Test
    void deleteUserById() {
        UserDto userDto = UserMapper.toUserDto(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        userService.deleteUser(userDto.getId());
        boolean exist =  userRepository.existsById(user.getId());

        assertFalse(exist);
    }

}