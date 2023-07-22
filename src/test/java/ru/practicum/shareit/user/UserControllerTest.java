package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    String BASE_URL = "/users";
    UserDto user;
    UserDto user2;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @BeforeEach
    void addUser() {
        user = UserDto.builder()
                .id(1L)
                .name("Java")
                .email("java@yandex.ru")
                .build();

        user2 = UserDto.builder()
                .id(1L)
                .name("UpdateJava")
                .email("Update@yandex.ru")
                .build();
    }

    @SneakyThrows
    @Test
    void createUser_whenIsValid_thenReturnOK() {
        when(userService.saveUser(user))
                .thenReturn(user);

        String result = mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(user), result);
    }

    @SneakyThrows
    @Test
    void createUser_whenNameIsEmpty_thenThrowBadRequest() {
        when(userService.saveUser(user))
                .thenReturn(user);
        user.setName(" ");

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, never())
                .saveUser(any());
    }

    @SneakyThrows
    @Test
    void createUser_whenEmailIsNull_thenThrowBadRequest() {
        when(userService.saveUser(user))
                .thenReturn(user);
        user.setEmail("");

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, never())
                .saveUser(any());
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserIsNotValid_thenReturnBadRequest() {
        Long userId = 0L;
        user.setName("TestJava");

        mockMvc.perform(put(BASE_URL + "/{userId}", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(userService, never()).updateUser(userId, user);
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserIsValid_thenReturnOK() {
        when(userService.updateUser(user2.getId(), user2)).thenReturn(user);

        mockMvc.perform(patch(BASE_URL + "/{userId}", user2.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @SneakyThrows
    @Test
    void updateUser_whenUserEmailIsValid_thenReturnOK() {
        user.setEmail("Update@yandex.ru");
        when(userService.updateUser(user2.getId(), user2)).thenReturn(user);

        mockMvc.perform(patch(BASE_URL + "/{userId}", user2.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @SneakyThrows
    @Test
    void getUserById_whenUserIdIsValid_thenReturnOK() {
        long userId = 1L;

        mockMvc.perform(get(BASE_URL + "/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());
        verify(userService).findUserById(userId);
    }

    @SneakyThrows
    @Test
    void getUserById_whenUserIdIsNotExist_thenReturnBadRequest() {
        when(userService.findUserById(999L))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(userService).findUserById(999L);
    }

    @Test
    void deleteUser_thenReturnOK() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void getAllUsers_whenUserListFiledUsers_thenReturnOK() {
        when(userService.findAllUsers()).thenReturn(List.of(user));
        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(1)),
                        jsonPath("$[0].name", is("Java")),
                        jsonPath("$[0].email", is("java@yandex.ru")));
        verify(userService, times(2)).findAllUsers();
    }

    @SneakyThrows
    @Test
    void getAllUsers_whenUserListIsEmpty_thenReturnEmptyListAndStatusOK() {
        when(userService.findAllUsers()).thenReturn(Collections.EMPTY_LIST);
        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(0)));
        verify(userService, times(2)).findAllUsers();
    }

}