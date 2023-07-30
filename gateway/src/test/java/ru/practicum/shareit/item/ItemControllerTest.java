package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class ItemControllerTest {

    String url = "/items";
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    ItemClient itemClient;

    @MockBean
    BookingClient bookingClient;
    @MockBean
    ItemRequestClient itemRequestClient;
    @MockBean
    UserClient userClient;
    ItemDto itemDto;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Java")
                .email("java@yandex.ru")
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Equipment")
                .description("Test Equipment desc")
                .available(true)
                .owner(userDto.getId())
                .requestId(null)
                .build();
    }

    @SneakyThrows
    @Test
    void createItem_whenItemNameIsEmpty_thenReturnObjectNotFound() {
        itemDto.setName(" ");
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("name")))
                .andExpect(jsonPath("$[0].message", is("must not be blank")));
    }

    @SneakyThrows
    @Test
    void createItem_whenItemAvailableIsEmpty_thenReturnBadRequest() {
        itemDto.setAvailable(null);
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("available")))
                .andExpect(jsonPath("$[0].message", is("must not be null")));

    }

    @SneakyThrows
    @Test
    void createItem_whenItemDescriptionIsEmpty_thenReturnBadRequest() {
        itemDto.setDescription(" ");

        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("description")))
                .andExpect(jsonPath("$[0].message", is("must not be blank")));
    }


    @SneakyThrows
    @Test
    void updateItem_whenUserIdIsEmpty_thenReturnInternalServerError() {

        mockMvc.perform(patch(url + "/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"Required request header 'X-Sharer-User-Id' " +
                        "for method parameter type Long is not present\"}"));
    }

    @SneakyThrows
    @Test
    void createCommentItem_whenCommentEmptyText_ReturnBadRequest() {
        CommentDto commentDto = CommentDto.builder().text("").build();
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("text")))
                .andExpect(jsonPath("$[0].message", is("Добавьте текст комментария")));
    }

    @SneakyThrows
    @Test
    void getRequests_whenFromIsNegative_ReturnInternalServerError() {
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @SneakyThrows
    @Test
    void getRequests_whenSizeIsNegative_ReturnInternalServerError() {
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError());
    }

}