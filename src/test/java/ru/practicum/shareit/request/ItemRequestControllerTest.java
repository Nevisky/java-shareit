package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItems;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class ItemRequestControllerTest {

    @MockBean
    ItemService itemService;
    @MockBean
    ItemRequestService itemRequestService;
    @MockBean
    UserService userService;
    Booking booking;
    User booker;
    User user;
    User owner;
    Item item;
    String url = "/requests";
    ItemRequest itemRequest;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @MockBean
    private BookingService bookingService;

    @BeforeEach
    void setUp() {

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("TestEq")
                .created(LocalDateTime.now())
                .requestor(booker)
                .build();

        owner = User.builder()
                .id(2L)
                .name("TestOwner")
                .email("owner@mail.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Equipment")
                .description("Test Equipment Description")
                .owner(owner)
                .available(true)
                .request(itemRequest)
                .build();

        user = User.builder()
                .id(1L)
                .name("TestUser")
                .email("user@yandex.ru")
                .build();

        booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(10))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @SneakyThrows
    @Test
    void createRequest_thenReturnOK() {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        when(itemRequestService.createRequest(itemRequestDto, 1L)).thenReturn(itemRequestDto);
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemRequestDto)));
    }

    @SneakyThrows
    @Test
    void createRequestWithNotFoundUser_thenReturnNotFound() {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.createRequest(itemRequestDto, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", 999L)));
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Пользователь не найден: id=999\"}"));
    }

    @SneakyThrows
    @Test
    void createRequest_whenRequestHasEmptyDescription_thenReturnBadRequest() {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setDescription("");
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("description")))
                .andExpect(jsonPath("$[0].message", is("Добавьте описание")));
    }

    @SneakyThrows
    @Test
    void getAllRequestsWithUsers_thenReturnList() {
        Collection<ItemDto> itemDtoCollection = List.of(ItemMapper.toItemDto(item));
        ItemRequestWithItems itemRequestWithItems = ItemRequestMapper.toItemRequestWithItems(itemRequest, itemDtoCollection);
        when(itemRequestService.getItemsRequestByRequestorId(user.getId())).thenReturn(List.of(itemRequestWithItems));
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @SneakyThrows
    @Test
    void getAllRequestsWithUsers_thenReturnEmptyList() {
        when(itemRequestService.getItemsRequestByRequestorId(user.getId())).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @SneakyThrows
    @Test
    void getAllRequestsWithUsersWithNotFoundUser_thenReturnNotFound() {
        when(itemRequestService.getItemsRequestByRequestorId(999L))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", 999L)));
        mockMvc.perform(get(url)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Пользователь не найден: id=999\"}"));
    }

    @SneakyThrows
    @Test
    void getRequestWithUserById_thenReturnOK() {
        Collection<ItemDto> itemDtoCollection = List.of(ItemMapper.toItemDto(item));
        ItemRequestWithItems itemRequestWithItems = ItemRequestMapper.toItemRequestWithItems(itemRequest, itemDtoCollection);

        when(itemRequestService.getRequestById(user.getId(), itemRequest.getId())).thenReturn(itemRequestWithItems);
        mockMvc.perform(get(url + "/1")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemRequestWithItems)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemRequestWithItems)));
    }

    @SneakyThrows
    @Test
    void getRequestWithUsersById_whenNotExistingUserId_thenReturnNotFound() {
        when(itemRequestService.getRequestById(999L, 1L))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", 999L)));
        mockMvc.perform(get(url + "/1")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Пользователь не найден: id=999\"}"));
    }

    @SneakyThrows
    @Test
    void getRequestWithUsersById_whenNotExistingRequestId_thenReturnNotFound() {
        when(itemRequestService.getRequestById(1L, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Запрос не найден: id=%d", 999L)));
        mockMvc.perform(get(url + "/999")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Запрос не найден: id=999\"}"));
    }

    @SneakyThrows
    @Test
    void getRequests_thenReturnStatusOk() {
        Collection<ItemDto> itemDtoCollection = List.of(ItemMapper.toItemDto(item));
        ItemRequestWithItems itemRequestWithItems = ItemRequestMapper.toItemRequestWithItems(itemRequest, itemDtoCollection);
        when(itemRequestService.getAllRequestsByPageable(1L, 0, 1))
                .thenReturn(List.of(itemRequestWithItems));
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @SneakyThrows
    @Test
    void getRequests_whenFromIsNegative_ReturnInternalServerError() {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @SneakyThrows
    @Test
    void getRequests_whenSizeIsZero_ReturnInternalServerError() {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @SneakyThrows
    @Test
    void getRequests_whenSizeIsNegative_ReturnInternalServerError() {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

}