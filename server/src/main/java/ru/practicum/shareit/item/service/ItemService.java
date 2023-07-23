package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;

public interface ItemService {

    ItemDto saveItem(Long id, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId);

    ItemDtoWithBookingAndComments getItemById(Long itemId, Long userId);

    List<ItemDtoWithBookingAndComments> getItemDtoByUserId(Long userId, int from, int size);

    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);

    Collection<ItemDto> getItemsDtoByRequest(String text, int from, int size);

    User validateUser(Long userId);

    Item validateItem(Long itemId);

}