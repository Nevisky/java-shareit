package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDto;


import java.time.LocalDateTime;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@WebMvcTest
class BookingControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	BookingClient bookingClient;
	@MockBean
	ItemClient itemClient;
	@MockBean
	ItemRequestClient itemRequestClient;
	@MockBean
	UserClient userClient;

	@Autowired
	ObjectMapper objectMapper;

	String url = "/bookings";

	UserDto.UserDtoBuilder userDtoBuilder;
	ItemDto.ItemDtoBuilder itemDtoBuilder;
	BookingResponse.BookingResponseBuilder bookingResponseBuilder;
	BookingDto.BookingDtoBuilder bookingDtoBuilder;


	@BeforeEach
	void setUp() {
		LocalDateTime now = LocalDateTime.now();
		userDtoBuilder = UserDto.builder()
				.id(1L)
				.name("name")
				.email("email@email.ru");
		itemDtoBuilder = ItemDto.builder()
				.id(1L)
				.name("name")
				.description("description")
				.available(true);
		bookingResponseBuilder = BookingResponse.builder()
				.id(1L)
				.booker(userDtoBuilder.build())
				.item(itemDtoBuilder.build())
				.start(now.plusMinutes(1))
				.end(now.plusMinutes(2))
				.status(BookingStatus.WAITING);

		bookingDtoBuilder = BookingDto.builder()
				.id(1L)
				.bookerId(userDtoBuilder.build().getId())
				.itemId(itemDtoBuilder.build().getId())
				.start(now.plusMinutes(1))
				.end(now.plusMinutes(2))
				.status(BookingStatus.WAITING);
	}

	@SneakyThrows
	@Test
	void createBooking_whenItemIdNotFound_thenReturnBadRequest() {
		BookingDto bookingDto =  bookingDtoBuilder.itemId(null).build();
		mockMvc.perform(post(url)
						.contentType("application/json")
						.header("X-Sharer-User-Id", 1)
						.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].code", is(400)))
				.andExpect(jsonPath("$[0].fieldName", is("itemId")))
				.andExpect(jsonPath("$[0].message", is("must not be null")));

	}

	@SneakyThrows
	@Test
	void createBooking_whenStartTimeIsNull_thenReturnBadRequest() {
		BookingDto bookingDto =  bookingDtoBuilder.start(null).build();
		mockMvc.perform(post(url)
						.contentType("application/json")
						.header("X-Sharer-User-Id", 1)
						.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].code", is(400)))
				.andExpect(jsonPath("$[0].fieldName", is("start")))
				.andExpect(jsonPath("$[0].message", is("must not be null")));

	}

	@SneakyThrows
	@Test
	void createBooking_whenEndTimeIsNull_thenReturnBadRequest() {
		BookingDto bookingDto =  bookingDtoBuilder.end(null).build();
		mockMvc.perform(post(url)
						.contentType("application/json")
						.header("X-Sharer-User-Id", 1)
						.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].code", is(400)))
				.andExpect(jsonPath("$[0].fieldName", is("end")))
				.andExpect(jsonPath("$[0].message", is("must not be null")));

	}

	@SneakyThrows
	@Test
	void getBookingsPresentUser_whenFromIsNegative_ReturnInternalServerError() {
		mockMvc.perform(get(url)
						.header("X-Sharer-User-Id", 1)
						.param("state", "WAITING")
						.param("from", "-1")
						.param("size", "10"))
				.andExpect(status().isInternalServerError());
	}

	@SneakyThrows
	@Test
	void getBookingsPresentUser_whenSizeIsNegative_ReturnInternalServerError() {
		mockMvc.perform(get(url)
						.header("X-Sharer-User-Id", 1)
						.param("state", "WAITING")
						.param("from", "1")
						.param("size", "0"))
				.andExpect(status().isInternalServerError());
	}

}