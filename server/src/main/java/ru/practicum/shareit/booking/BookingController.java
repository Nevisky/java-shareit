package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.Collection;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponse create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @RequestBody BookingDto bookingDto) {
        log.info("Запрос на создание бронирования");
        return bookingService.saveBooking(userId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public BookingResponse updateBookingStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable("bookingId") Long bookingId,
                                  @RequestParam boolean approved) {
        return bookingService.updateBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse findByBookingId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable("bookingId") Long bookingId) {
        log.info("Получение бронирования по id={}", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingResponse> findAllUsersBookingByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                  @RequestParam(defaultValue = "ALL") String state,
                                                                  @RequestParam(defaultValue = "0")
                                                                  @Min(value = 0, message = "Меньше нуля") int from,
                                                                  @RequestParam(defaultValue = "20")
                                                                  @Positive int size) {
        log.info("Получение списка всех бронирований пользователя id={}", userId);
        return bookingService.getAllUsersBookingByState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> findAllBookingsForItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                     @RequestParam(defaultValue = "ALL") String state,
                                                                     @RequestParam(defaultValue = "0")
                                                                         @Min(value = 0, message = "Меньше нуля") int from,
                                                                     @RequestParam(defaultValue = "10")
                                                                     @Positive int size) {
        log.info("Получение списка бронирований для всех вещей пользователя id={}",userId);
        return bookingService.getAllBookingsForItemsOfUser(userId, state, from, size);
    }

}