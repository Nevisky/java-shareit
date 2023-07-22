package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentsRepository;

    @Override
    public ItemDto saveItem(Long userId, ItemDto itemDto) {
        if (itemDto.getName().isEmpty() || itemDto.getAvailable() == null || itemDto.getDescription() == null) {
            throw new ValidationException("Значение поля не задано");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new ObjectNotFoundException("Запрашиваемого пользователя не существует");
        }
        User user = validateUser(userId);
        itemDto.setOwner(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) {
        User user = validateUser(userId);
        Item excistedItem = itemRepository.findById(itemId).orElseThrow();
        if (itemRepository.findById(itemId).orElseThrow().getOwner() == null) {
            throw new ObjectNotFoundException("Данная вещь не принадлежит пользователю");
        }
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }
        Item item = Item.builder()
                .id(itemId)
                .name(itemDto.getName() != null ? itemDto.getName() : excistedItem.getName())
                .description(itemDto.getDescription() != null ? itemDto.getDescription() : excistedItem.getDescription())
                .available(itemDto.getAvailable() != null ? itemDto.getAvailable() : excistedItem.getAvailable())
                .owner(itemDto.getOwner() != null ? user : excistedItem.getOwner())
                .build();
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoWithBookingAndComments getItemById(Long itemId, Long userId) {
        Item item = validateItem(itemId);
        User user = validateUser(userId);
        List<Booking> bookings = bookingRepository.findByItemId(itemId);
        List<Comment> comments = commentsRepository.findByItemId(itemId);

        return toItemDtoWBAC(item, user, bookings, comments);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDtoWithBookingAndComments> getItemDtoByUserId(Long userId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("id"));
        User user = validateUser(userId);
        List<Item> usersItems = itemRepository.findItemByOwnerId(user.getId(), pageRequest);
        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
        List<Comment> comments = commentsRepository.findByItemIdIn(usersItems.stream()
                .map(Item::getId).collect(Collectors.toList()));

        return usersItems.stream().map(Item -> ItemMapper.toItemDtoWBAC(Item, user, bookings, comments)).collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        User user = validateUser(userId);
        Item item = validateItem(itemId);
        Booking booking = bookingRepository.findTopByStatusNotLikeAndItemIdAndBookerIdOrderByEndAsc(BookingStatus.REJECTED, itemId, userId);
        Comment comment = CommentMapper.toComment(commentDto, user, item);
        if (booking == null) {
            throw new ValidationException(String.format("Предмет с id = %d не был забронирован пользователем с id = %d", itemId, userId));
        }
        if (booking.getStart().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Неправильная дата бронирования");
        }
        return CommentMapper.commentDto(commentsRepository.save(comment));
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> getItemsDtoByRequest(String text, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("id"));
        if (text.isBlank() || text.isEmpty()) {
            return Collections.emptyList();
        }
        return ItemMapper.listToItemDto(itemRepository.search(text,pageRequest));
    }
    @Override
    public User validateUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(String.format(
                "Пользователь с id = %d не найден", userId)));
    }
    @Override
    public Item validateItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException(String.format(
                "Предмет с id = %d не найден", itemId)));
    }

}