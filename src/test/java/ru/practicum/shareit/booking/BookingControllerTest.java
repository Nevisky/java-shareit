package ru.practicum.shareit.booking_example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking_example.dto.BookingDto;
import ru.practicum.shareit.booking_example.dto.BookingResponse;
import ru.practicum.shareit.booking_example.mapper.BookingMapper;
import ru.practicum.shareit.booking_example.model.Booking;
import ru.practicum.shareit.booking_example.service.BookingService;
import ru.practicum.shareit.booking_example.util.BookingStatus;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidateStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class BookingControllerTest {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MockMvc mockMvc;

	@MockBean
	BookingService bookingService;

	@MockBean
	ItemService itemService;

	@MockBean
	UserService userService;

	@MockBean
	ItemRequestService itemRequestService;

	Booking booking;

	User booker;

	User owner;

	Item item;

	ItemRequest itemRequest;

	String url = "/bookings";

	@BeforeEach
	void setUp() {

		itemRequest = ItemRequest.builder()
                .id(1L).description("TestEq")
                .created(LocalDateTime.now())
                .requestor(booker).build();

		owner = User.builder()
                .id(2L).name("TestOwnew").email("owner@mail.ru").build();

		item = Item.builder()
				.id(1L)
				.name("Test Equipment")
				.description("Test Equipment Description")
				.owner(owner).available(true)
				.request(itemRequest)
				.build();

		booker = User.builder()
				.id(1L)
				.name("TestBooker")
				.email("booker@yandex.ru")
				.build();

		booking = Booking.builder()
				.id(1L).booker(booker)
				.item(item)
				.start(LocalDateTime.now())
				.end(LocalDateTime.now())
				.status(BookingStatus.APPROVED)
				.build();
	}

	@SneakyThrows
	@Test
	void createBooking_whenBookingIsValid_thenReturnOK() {
		BookingDto bookingDto = BookingMapper.toBookingDto(booking);
		BookingResponse bookingResponse = BookingMapper.toBookingResponse(booking);

		when(bookingService.saveBooking(bookingResponse.getId(), bookingDto)).thenReturn(bookingResponse);

		String result = mockMvc.perform(post(url)
				.contentType("application/json")
				.header("X-Sharer-User-Id", 1)
				.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn().getResponse()
				.getContentAsString();


		assertEquals(objectMapper.writeValueAsString(bookingResponse), result);
	}

	@SneakyThrows
	@Test
	void createBooking_whenUserIdNotFound_thenReturnObjectNotFound() {
		BookingDto bookingDto = BookingMapper.toBookingDto(booking);
		bookingDto.setBookerId(null);

		when(bookingService.saveBooking(999L, bookingDto)).thenThrow(new ObjectNotFoundException("Пользоавтеля не существует"));

		mockMvc.perform(post(url)
				.contentType("application/json")
				.header("X-Sharer-User-Id", 999)
				.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status()
						.isNotFound());

		verify(bookingService, never()).saveBooking(bookingDto.getId(), bookingDto);
	}

	@SneakyThrows
	@Test
	void createBooking_whenItemIdNotFound_thenReturnObjectNotFound() {
		BookingDto bookingDto = BookingMapper.toBookingDto(booking);
		bookingDto.setItemId(999L);

		when(bookingService.saveBooking(1L, bookingDto)).thenThrow(new ObjectNotFoundException("Предмета не существует"));

		mockMvc.perform(post(url)
				.contentType("application/json")
				.header("X-Sharer-User-Id", 1)
				.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status()
						.isNotFound());

	}

	@SneakyThrows
	@Test
	void createBooking_whenEndTimeInPast_thenReturnInternalServerError() {
		BookingDto bookingDto = BookingMapper.toBookingDto(booking);
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.minusDays(4);
		bookingDto.setEnd(end);

		when(bookingService.saveBooking(1L, bookingDto))
				.thenThrow(new IllegalArgumentException("Время завершения бронирования не может быть раньше начала"));
		mockMvc.perform(post(url)
				.contentType("application/json")
				.header("X-Sharer-User-Id", 1)
				.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print())
				.andExpect(status().isInternalServerError());

		assertTrue(bookingDto.getStart().isAfter(bookingDto.getEnd()));
	}

	@SneakyThrows
	@Test
	void createBooking_whenEndTimeIsNull_thenReturnBadRequest() {
		BookingDto bookingDto = BookingMapper.toBookingDto(booking);
		bookingDto.setEnd(null);

		when(bookingService.saveBooking(1L, bookingDto))
				.thenThrow(new ValidateStateException("Время завершения бронирования не может быть NULL"));
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

		assertNull(bookingDto.getEnd());
	}

	@SneakyThrows
	@Test
	void createBooking_whenStartTimeIsNull_thenReturnBadRequest() {
		BookingDto bookingDto = BookingMapper.toBookingDto(booking);
		bookingDto.setStart(null);

		when(bookingService.saveBooking(1L, bookingDto))
				.thenThrow(new ValidateStateException("Время начала бронирования не может быть NULL"));
		mockMvc.perform(post(url)
				.contentType("application/json")
				.header("X-Sharer-User-Id", 1)
				.content(objectMapper.writeValueAsString(bookingDto)))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].code", is(400)))
				.andExpect(jsonPath("$[0].fieldName", is("start")))
				.andExpect(jsonPath("$[0].message", is("must not be null")));

		assertNull(bookingDto.getStart());
	}

	@SneakyThrows
	@Test
	void updateBookingStatus_whenApproveIsTrue_ReturnOK() {
		BookingResponse bookingResponse = BookingMapper.toBookingResponse(booking);
		when(bookingService.updateBookingStatus(1L, 1L, true)).thenReturn(bookingResponse);

		mockMvc.perform(patch(url + "/1")
				.header("X-Sharer-User-Id", 1)
				.param("approved", "true"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(bookingResponse)));
	}

	@SneakyThrows
	@Test
	void updateBookingStatus_whenApproveIfFalse_ReturnOK() {
		BookingResponse bookingResponse = BookingMapper.toBookingResponse(booking);
		bookingResponse.setStatus(BookingStatus.REJECTED);

		when(bookingService.updateBookingStatus(1L, 1L, false)).thenReturn(bookingResponse);

		mockMvc.perform(patch(url + "/1")
				.header("X-Sharer-User-Id", 1)
				.param("approved", "false"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(bookingResponse)));
	}

	@SneakyThrows
	@Test
	void shouldApproveWithNotFoundUser_ReturnStatusNotFound() {
		when(bookingService.updateBookingStatus(999L, 1L, true))
				.thenThrow(new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", 999L)));
		mockMvc.perform(patch(url + "/1")
				.header("X-Sharer-User-Id", 999)
				.param("approved", "true"))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().json("{\"message\":\"Пользователь не найден: id=999\"}"));
	}

	@SneakyThrows
	@Test
	void updateBookingStatus_whenApproveWithNotFoundBooking_ReturnNotFound() {
		when(bookingService.updateBookingStatus(1L, 999L, true))
				.thenThrow(new ObjectNotFoundException(String.format("Бронирование не найдено: id=%d", 999L)));
		mockMvc.perform(patch(url + "/999")
				.header("X-Sharer-User-Id", 1)
				.param("approved", "true"))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().json("{\"message\":\"Бронирование не найдено: id=999\"}"));
	}

	@SneakyThrows
	@Test
	void updateBookingStatus_whenApproveWithStatusNotWaiting_ReturnStatusBadRequest() {
		when(bookingService.updateBookingStatus(1L, 1L, false))
				.thenThrow(new ValidationException(String.format("Нельзя забронировать: id=%d", 1L)));
		mockMvc.perform(patch(url + "/1")
				.header("X-Sharer-User-Id", 1)
				.param("approved", "false"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{\"message\":\"Нельзя забронировать: id=1\"}"));
	}

	@SneakyThrows
	@Test
	void getBookingById_thenReturnOK() {
		BookingResponse bookingResponse = BookingMapper.toBookingResponse(booking);

		when(bookingService.getBookingById(1L, 1L)).thenReturn(bookingResponse);
		mockMvc.perform(get(url + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Sharer-User-Id", 1)
				.content(objectMapper.writeValueAsString(bookingResponse)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(bookingResponse)));
	}

	@SneakyThrows
	@Test
	void getBookingByIdWhenNotExistingUserId_ReturnBadRequest() {
		when(bookingService.getBookingById(999L, 1L))
				.thenThrow(new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", 999L)));
		mockMvc.perform(get(url + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Sharer-User-Id", 999))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().json("{\"message\":\"Пользователь не найден: id=999\"}"));
	}

	@SneakyThrows
	@Test
	void getBookingById_whenNotExistingBookingId_ReturnBadRequest() {
		when(bookingService.getBookingById(1L, 999L))
				.thenThrow(new ObjectNotFoundException(String.format("Бронирование не найдено: id=%d", 999L)));
		mockMvc.perform(get(url + "/999")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Sharer-User-Id", 1))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().json("{\"message\":\"Бронирование не найдено: id=999\"}"));
	}

	@SneakyThrows
	@Test
	void getBookingsPresentUser_ReturnEmptyList() {
		when(bookingService.getAllBookingsForItemsOfUser(1L, "APPROVE", 0, 1))
				.thenReturn(Collections.emptyList());
		mockMvc.perform(get(url)
				.header("X-Sharer-User-Id", 1)
				.param("state", "rejected"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@SneakyThrows
	@Test
	void getBookingsPresentUser_ReturnListBookings() {
		BookingResponse bookingResponse = BookingMapper.toBookingResponse(booking);
		bookingResponse.setStatus(BookingStatus.WAITING);
		when(bookingService.getAllUsersBookingByState(1L, "WAITING", 0, 10))
				.thenReturn(List.of(bookingResponse));
		mockMvc.perform(get(url)
				.header("X-Sharer-User-Id", 1)
				.param("state", "WAITING")
				.param("from", "0")
				.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$.size()", is(1)));
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
	void getBookingsPresentUser_whenStateFail_ReturnInternalServerError() {
		when(bookingService.getAllUsersBookingByState(1L, "WRONG", 1, 10))
				.thenThrow(new IllegalArgumentException(String.format("Unknown state: %s", "WRONG")));
		mockMvc.perform(get(url)
				.header("X-Sharer-User-Id", 1)
				.param("state", "WRONG")
				.param("from", "1")
				.param("size", "10"))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(content().json("{\"message\":\"Unknown state: WRONG\"}"));
	}

	@SneakyThrows
	@Test
	void getBookingsAllItemPresentUser_thenReturnEmptyList() {
		when(bookingService.getAllBookingsForItemsOfUser(1L, "APPROVED", 1, 10))
				.thenReturn(Collections.emptyList());
		mockMvc.perform(get(url + "/owner")
				.header("X-Sharer-User-Id", 1)
				.param("state", "APPROVED")
				.param("from", "1")
				.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@SneakyThrows
	@Test
	void getBookingsAllItemPresentUser_thenReturnListBookings() {
		BookingResponse bookingResponse = BookingMapper.toBookingResponse(booking);
		when(bookingService.getAllBookingsForItemsOfUser(1L, "APPROVED", 1, 10))
				.thenReturn(List.of(bookingResponse));
		mockMvc.perform(get(url + "/owner")
				.header("X-Sharer-User-Id", 1)
				.param("state", "APPROVED")
				.param("from", "1")
				.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$.size()", is(1)));
	}

	@SneakyThrows
	@Test
	void getBookingsAllItemPresentUser_whenFromIsNegative_ReturnInternalServerError() {
		mockMvc.perform(get(url + "/owner")
				.header("X-Sharer-User-Id", 1)
				.param("state", "APPROVED")
				.param("from", "-1")
				.param("size", "10"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json("{\"message\":\"findAllBookingsForItemsOfUser.from: Меньше нуля\"}"));
	}

	@SneakyThrows
	@Test
	void getBookingsAllItemPresentUser_whenStateWrong_ReturnInternalServerError() {
		when(bookingService.getAllBookingsForItemsOfUser(1L, "FAIL", 0, 10))
				.thenThrow(new ValidateStateException(String.format("Unknown state: %s", "UNSUPPORTED_STATUS")));
		mockMvc.perform(get(url + "/owner")
				.header("X-Sharer-User-Id", 1)
				.param("state", "FAIL")
				.param("from", "0")
				.param("size", "10"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{\"error\":\"Unknown state: UNSUPPORTED_STATUS\"}"));
	}

}