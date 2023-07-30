package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.ObjectNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

    Booking booking;

    User booker;

    User owner;

    Item item;

    ItemRequest itemRequest;
    Sort sort = Sort.by(Sort.Direction.DESC, "start");
    PageRequest page = PageRequest.of(0, 1, sort);

    @BeforeEach
    void setUp() {

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("TestEq")
                .created(LocalDateTime.now())
                .requestor(booker).build();

        owner = User.builder()
                .id(2L)
                .name("TestOwner")
                .email("owner@mail.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Equipment")
                .description("Test Equipment Description")
                .owner(owner).available(true)
                .request(itemRequest).build();

        booker = User.builder()
                .id(1L)
                .name("TestBooker")
                .email("booker@yandex.ru")
                .build();

        booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusMinutes(10))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void getBookingsPresentUser_WhenBookingHasDifferentState_thenReturnBooking() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        // All
        when(bookingRepository.findByBookerIdOrderByStartDesc(userId, page)).thenReturn(List.of(booking));
        Collection<BookingResponse> bookingResponses = bookingService.getAllUsersBookingByState(userId, "ALL", from, size);

        assertNotNull(bookingResponses);
        assertEquals(1, bookingResponses.size());
        assertEquals(booking.getId(), bookingResponses.stream().map(BookingResponse::getId).collect(Collectors.toList()).get(0));

        // CURRENT
        booking.setEnd(LocalDateTime.now().plusMinutes(60));

        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingResponses = bookingService.getAllUsersBookingByState(userId, "CURRENT", from, size);

        assertNotNull(bookingResponses);
        assertEquals(1, bookingResponses.size());

        // PAST
        when(bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        bookingResponses = bookingService.getAllUsersBookingByState(userId, "PAST", from, size);

        assertNotNull(bookingResponses);
        assertEquals(1, bookingResponses.size());

        // FUTURE
        booking.setStart(LocalDateTime.now().plusSeconds(60));

        when(bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        bookingResponses = bookingService.getAllUsersBookingByState(userId, "FUTURE", from, size);

        assertNotNull(bookingResponses);
        assertEquals(1, bookingResponses.size());

        // WAITING
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        bookingResponses = bookingService.getAllUsersBookingByState(userId, "WAITING", from, size);

        assertNotNull(bookingResponses);
        assertEquals(1, bookingResponses.size());

        // REJECTED
        booking.setStatus(BookingStatus.REJECTED);

        bookingResponses = bookingService.getAllUsersBookingByState(userId, "REJECTED", from, size);

        assertNotNull(bookingResponses);
        assertEquals(1, bookingResponses.size());

        // WRONG
        String error = "Unknown state: UNSUPPORTED_STATUS";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllUsersBookingByState(userId, "Unknown state: UNSUPPORTED_STATUS", from, size));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void getAllBookingsForItemsOfUser_thenReturnBookings() {
        long userId = owner.getId();

        // ALL
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, page)).thenReturn(List.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        Collection<BookingResponse> bookings = bookingService.getAllBookingsForItemsOfUser(userId, "ALL", 0, 1);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.stream().map(BookingResponse::getId).collect(Collectors.toList()).get(0));

        // PAST
        when(bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        bookings = bookingService.getAllBookingsForItemsOfUser(userId, "PAST", 0, 1);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        // FUTURE
        booking.setStart(LocalDateTime.now().plusSeconds(60));
        when(bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        bookings = bookingService.getAllBookingsForItemsOfUser(userId, "FUTURE", 0, 1);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        // CURRENT
        booking.setEnd(LocalDateTime.now().plusSeconds(120));
        when(bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookings = bookingService.getAllBookingsForItemsOfUser(userId, "CURRENT", 0, 1);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        // WAITING
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookings = bookingService.getAllBookingsForItemsOfUser(userId, "WAITING", 0, 1);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        // REJECTED
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookings = bookingService.getAllBookingsForItemsOfUser(userId, "REJECTED", 0, 1);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        // Wrong State
        String error = "Unknown state: UNSUPPORTED_STATUS";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllUsersBookingByState(userId, "Unknown state: UNSUPPORTED_STATUS", 0, 1));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void getBookingByIdWithBooker_thenReturnBookingDto() {
        long bookerId = owner.getId();
        long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingResponse bookings = bookingService.getBookingById(bookerId, bookingId);
        assertNotNull(bookings);
        assertEquals(booking.getId(), bookings.getId());
    }

    @Test
    void getBookingById_whenWrongUser_ReturnObjectNotFoundException() {
        long userId = booker.getId();
        long bookingId = booking.getId();
        booker.setId(3223L);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        String error = "Запрашиваемая информация доступна только владельцу";
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void createBooking_thenReturnBookingResponse() {
        long bookerId = booker.getId();

        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        BookingResponse bookingResponse = bookingService.saveBooking(bookerId, bookingDto);

        assertNotNull(bookingResponse);
        assertEquals(booking.getId(), bookingResponse.getId());
    }

    @Test
    void createBooking_whenItemNotAvailable_thenReturnObjectNotFoundException() {
        item.setAvailable(false);
        long bookerId = booker.getId();
        long itemId = item.getId();
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        String error = String.format("Предмет с id = %d не найден.", itemId);

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.saveBooking(bookerId, bookingDto));
        assertEquals(error, ex.getMessage());
    }

    @Test
    void createBooking_whenOwnerItemIsWrong_thenReturnObjectNotFoundException() {
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        UserDto owner = UserMapper.toUserDto(booker);
        String error = String.format("Предмет с id = %d не найден.", item.getId());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.saveBooking(owner.getId(), bookingDto));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void tryApproveBooking_thenReturnUpdatedBookingDto() {
        long userId = booker.getId();
        long bookingId = booking.getId();
        item.setOwner(booker);
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponse bookingStatus = bookingService.updateBookingStatus(userId, bookingId, false);

        assertNotNull(bookingStatus);
        assertEquals(booking.getId(), bookingStatus.getId());
    }

    @Test
    void tryApproveBooking_whenBookingNotFound_thenReturnObjectNotFoundException() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = String.format("Бронирование с id = %d не найдено.", bookingId);

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateBookingStatus(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void tryApproveBooking_WhenNotOwner_thenReturnObjectNotFoundException() {
        long userId = booker.getId();
        long bookingId = booking.getId();
        String error = "Вы не являетесь владельцем предмета.";
        when(bookingRepository.findById(userId)).thenReturn(Optional.of(booking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateBookingStatus(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void tryApproveBooking_whenNotAvailable_thenReturnValidationException() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = String.format("Бронь с id = %d уже существует.",bookingId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }
}