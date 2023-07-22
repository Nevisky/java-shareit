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
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class ItemControllerTest {

    String url = "/items";
    User user;
    Item item;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserService userService;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private ItemRequestService itemRequestService;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private CommentRepository commentsRepository;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Java")
                .email("java@yandex.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Equipment")
                .description("Test Equipment desc")
                .available(true)
                .owner(user)
                .request(null)
                .build();
    }

    @SneakyThrows
    @Test
    void createItem_whenItemIsValid_thenReturnOK() {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        when(itemService.saveItem(item.getId(), itemDto)).thenReturn(itemDto);

        String result = mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals(objectMapper.writeValueAsString(itemDto), result);

    }

    @SneakyThrows
    @Test
    void createItem_whenHeaderIsEmpty_thenReturnInternalServerError() {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        when(itemService.saveItem(item.getId(), itemDto)).thenThrow(new IllegalArgumentException());

        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).saveItem(itemDto.getId(), itemDto);

    }

    @SneakyThrows
    @Test
    void createItem_whenUserNotFound_thenReturnObjectNotFound() {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setOwner(999L);
        when(itemService.saveItem(999L, itemDto)).thenThrow(new ObjectNotFoundException("Нет такого пользователя"));

        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @SneakyThrows
    @Test
    void createItem_whenItemNameIsEmpty_thenReturnObjectNotFound() {
        ItemDto itemDto = ItemMapper.toItemDto(item);
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
        ItemDto itemDto = ItemMapper.toItemDto(item);
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
        ItemDto itemDto = ItemMapper.toItemDto(item);
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
    void updateItem_whenItemIsAvailable_thenReturnOK() {
        ItemDto itemDto = ItemDto.builder().id(1L).name("Update").description("Update description")
                .available(true).build();
        ItemDto itemDtoResponse = ItemDto.builder().id(1L).name("Update").description("Update description")
                .available(true).build();

        when(itemService.updateItem(1L, itemDto, 1L)).thenReturn(itemDtoResponse);

        mockMvc.perform(patch(url + "/1")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDtoResponse)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemDtoResponse)));
    }

    @SneakyThrows
    @Test
    void updateItem_whenUserIdIsEmpty_thenReturnInternalServerError() {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        when(itemService.updateItem(null, itemDto, 1L)).thenThrow(new IllegalArgumentException("Такого пользователя не существует"));

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
    void getAllItems_whenNoUsers_thenReturnEmptyList() {

        when(itemService.getItemDtoByUserId(1L, 1, 1)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @SneakyThrows
    @Test
    void getItemById_whenItemIsValid_thenReturnOK() {
        ItemDtoWithBookingAndComments newDto = ItemDtoWithBookingAndComments.builder().id(1L).build();
        when(itemService.getItemById(1L, 1L)).thenReturn(newDto);
        mockMvc.perform(get(url + "/1")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(newDto)));
    }

    @SneakyThrows
    @Test
    void createCommentItem_whenCommentIsValid_thenReturnOK() {
        CommentDto commentDto = CommentDto.builder().id(1L).authorName("name").text("Comment").created(LocalDateTime.now()).build();

        when(itemService.createComment(commentDto, 1L, 1L)).thenReturn(commentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(commentDto)));
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
    void getItemsByTextRequest_thenReturnItem() {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        when(itemService.getItemsDtoByRequest(anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url + "/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        when(itemService.getItemsDtoByRequest(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));
        mockMvc.perform(get(url + "/search")
                        .param("text", "equ")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));
        verify(itemService, times(2))
                .getItemsDtoByRequest(anyString(), anyInt(), anyInt());
    }

}