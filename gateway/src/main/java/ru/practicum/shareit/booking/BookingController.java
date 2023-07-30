package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.util.BookingState;
import ru.practicum.shareit.exception.ValidateStateException;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody BookingDto bookingDto) {
        if (!(bookingDto.getStart().isAfter(LocalDateTime.now()) &&
                bookingDto.getEnd().isAfter(LocalDateTime.now()) &&
                bookingDto.getStart().isBefore(bookingDto.getEnd()))) {
            throw new ValidationException("Неправильно заданна дата");
        }
        log.info("Бронирование Booking={}", bookingDto.getId());
        return bookingClient.addBooking(userId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable("bookingId") Long bookingId,
                                  @RequestParam boolean approved) {
        log.info("Обновление статуса бронирования user{}", userId);
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findByBookingId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable("bookingId") Long bookingId) {
        log.info("Получение бронирования по id={}", bookingId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllUsersBookingByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                  @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                                  @RequestParam(defaultValue = "0")
                                                                  @Min(value = 0, message = "Меньше нуля") int from,
                                                                  @RequestParam(defaultValue = "20")
                                                                  @Positive int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new ValidateStateException("Unknown state: " + stateParam));
        log.info("Получение списка всех бронирований пользователя id={}", userId);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllBookingsForItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                     @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                                     @RequestParam(defaultValue = "0")
                                                                         @Min(value = 0, message = "Меньше нуля") int from,
                                                                     @RequestParam(defaultValue = "10")
                                                                     @Positive int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new ValidateStateException("Unknown state: " + stateParam));
        log.info("Получение списка бронирований для всех вещей пользователя id={}",userId);
        return bookingClient.getBookingsAllItem(userId, state, from, size);
    }

}