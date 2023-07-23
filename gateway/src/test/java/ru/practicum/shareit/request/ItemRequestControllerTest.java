package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserClient;

import java.time.LocalDateTime;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class ItemRequestControllerTest {

    ItemRequestDto itemRequestDto;
    String url = "/requests";


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


    @BeforeEach
    void setUp() {

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("TestEq")
                .created(LocalDateTime.now())
                .build();
    }

    @SneakyThrows
    @Test
    void createRequest_whenRequestHasEmptyDescription_thenReturnBadRequest() {
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
    void getRequests_whenFromIsNegative_ReturnInternalServerError() {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1"))
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