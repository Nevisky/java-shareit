package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto saveItem(Long id, ItemDto itemDto);

    ItemDto updateItem(Long userId,ItemDto itemDto,Long itemId);

    ItemDto getItemById(Long userId, Long itemId);

    Collection<ItemDto> search(String text);

    Collection<ItemDto> getAllItemsByUser(Long userId);

}
