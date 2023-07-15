package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.booking.util.State;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidateStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User validateUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                String.format("Пользователь с id = %d не найден", userId)));
    }

    private Item validateItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException(
                String.format("Предмет с id = %d не найден", itemId)));
    }

    private Booking validateBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new ObjectNotFoundException(String.format(
                "Бронирование с id = %d не найдено",bookingId)));
    }

    @Override
    public BookingResponse saveBooking(Long userId, BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        Item item = validateItem(bookingDto.getItemId());
        User user = validateUser(userId);

        if (!end.isAfter(start) || end.equals(start) || start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Неправильно указана дата");
        }
        if (!validateItem(bookingDto.getItemId()).getAvailable()) {
            throw new ValidationException("Предмет не доступен для бронироавния");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Нельзя забронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingResponse(bookingRepository.save(booking));

    }

    @Override
    public BookingResponse updateBookingStatus(Long userId, Long bookingId, boolean approved) {
        Booking booking = validateBooking(bookingId);
        if (approved && booking.getStatus() == BookingStatus.APPROVED) {
            throw new ValidationException(String.format("Бронь с id = %d уже существует",booking.getItem().getId()));
        }
        if (!approved && booking.getStatus() == BookingStatus.REJECTED) {
            throw new ValidationException(String.format("Бронь с id = %d уже отмеенна",booking.getItem().getId()));
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Нельзя обновить статус другого пользователя");
        }

        bookingRepository.save(booking);
        return BookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long userId, Long bookingId) {
        Booking booking = validateBooking(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (userId.equals(bookerId) || userId.equals(ownerId)) {
            return BookingMapper.toBookingResponse(booking);
        } else {
            throw new ObjectNotFoundException("Запрашиваемая информация доступна только владельцу");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingResponse> getAllUsersBookingByState(Long userId, String state, int from, int size) {
        if (from < 0) {
            throw new ValidationException("Заданная страница меньше 0");
        }
        validateUser(userId);
        State transformState = changeStringToState(state);
        LocalDateTime currentTime = LocalDateTime.now();
        Collection<Booking> usersBooking = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"start");
        PageRequest page = PageRequest.of(from / size, size, sort);
        switch (transformState) {
            case ALL:
                usersBooking = bookingRepository.findByBookerIdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                usersBooking = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId, currentTime, currentTime, page);
                break;
            case PAST:
                usersBooking = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, currentTime, page);
                break;
            case FUTURE:
                usersBooking = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, currentTime, page);
                break;
            case WAITING:
                usersBooking = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                usersBooking = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, page);
                break;
            case UNSUPPORTED_STATUS:
                throw new ValidateStateException("Unknown state: UNSUPPORTED_STATUS");
        }
        return usersBooking.stream().map(BookingMapper::toBookingResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingResponse> getAllBookingsForItemsOfUser(Long userId, String state, int from, int size) {
        if (from < 0) {
            throw new ValidationException("Заданная страница меньше 0");
        }
        validateUser(userId);
        State transformState = changeStringToState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"start");
        PageRequest page = PageRequest.of(from / size, size, sort);
        switch (transformState) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId, now, now, page);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, now, page);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId, now, page);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, page);
                break;
            case UNSUPPORTED_STATUS:
                throw new ValidateStateException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().map(BookingMapper::toBookingResponse).collect(Collectors.toList());
    }

    private State changeStringToState(String state) {
        try {
            return State.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

}