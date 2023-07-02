package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemRepository {

    Item save(Long userId,Item item);

    Item update(Long userId, Item item);

    Item findItemById(Long id);

    Collection<Item> findItemFromText(String text);

    Collection<Item> findAllItemsByUser(Long userId);

    Map<Long, Collection<Long>> getUsersItems();

}
