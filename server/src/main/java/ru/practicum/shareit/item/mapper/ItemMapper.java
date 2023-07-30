package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner().getId())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(ItemDto dto, User user) {

        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwner(user);
        if (dto.getRequestId() != null) {
            ItemRequest itemRequest = new ItemRequest();
            itemRequest.setId(dto.getRequestId());
            item.setRequest(itemRequest);
        }
        return item;
    }

    public static Collection<ItemDto> listToItemDto(Collection<Item> items) {
        Collection<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : items) {
            itemsDto.add(toItemDto(item));
        }
        return itemsDto;
    }

    public static ItemDtoWithBookingAndComments toItemDtoWBAC(Item item, User user, List<Booking> bookings, List<Comment> comments) {
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED)
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .filter(booking -> booking.getItem().getOwner().getId().equals(user.getId()))
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .max(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED)
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getItem().getOwner().getId().equals(user.getId()))
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        List<CommentDto> dtoCommentsList = comments.stream()
                .map(CommentMapper::commentDto).collect(Collectors.toList());

        return ItemDtoWithBookingAndComments.builder()
                .id(item.getId()).name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null)
                .nextBooking(nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null)
                .comments(dtoCommentsList).build();
    }

}