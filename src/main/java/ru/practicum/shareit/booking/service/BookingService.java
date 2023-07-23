package ru.practicum.shareit.booking_example.service;

import ru.practicum.shareit.booking_example.dto.BookingDto;
import ru.practicum.shareit.booking_example.dto.BookingResponse;

import java.util.Collection;

public interface BookingService {

    BookingResponse saveBooking(Long userId, BookingDto bookingDto);

    BookingResponse updateBookingStatus(Long userId, Long bookingId, boolean approved);

    BookingResponse getBookingById(Long userId, Long bookingId);

    Collection<BookingResponse> getAllUsersBookingByState(Long userId, String state, int from, int size);

    Collection<BookingResponse> getAllBookingsForItemsOfUser(Long userId, String state, int from, int size);

}