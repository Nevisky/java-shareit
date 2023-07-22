package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;

import java.util.Collection;

public interface BookingService {

    BookingResponse saveBooking(Long userId, BookingDto bookingDto);

    BookingResponse updateBookingStatus(Long userId, Long bookingId, boolean approved);

    BookingResponse getBookingById(Long userId, Long bookingId);

    Collection<BookingResponse> getAllUsersBookingByState(Long userId, String state, int from, int size);

    Collection<BookingResponse> getAllBookingsForItemsOfUser(Long userId, String state, int from, int size);

}