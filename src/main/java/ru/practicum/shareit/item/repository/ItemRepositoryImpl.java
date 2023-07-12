package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, Collection<Long>> usersItems = new HashMap<>();
    private Long id = 0L;

    public Map<Long, Collection<Long>> getUsersItems() {
        return usersItems;
    }

    public long getId() {
        return ++id;
    }

    @Override
    public Item save(Long userId, Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        usersItems.put(userId, Collections.singleton(item.getId()));
        return item;
    }

    @Override
    public Item update(Long userId, Item item) {
        items.put(item.getId(), item);
        usersItems.put(userId, Collections.singleton(item.getId()));
        return item;
    }

    @Override
    public Item findItemById(Long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> findItemFromText(String text) {
        return items.values().stream().filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase())
                || item.getDescription().toLowerCase().contains((text.toLowerCase()))))
                .filter(Item::getAvailable).collect(Collectors.toList());
    }

    @Override
    public Collection<Item> findAllItemsByUser(Long userId) {
        Collection<Item> listOfItems = new ArrayList<>();
        usersItems.get(userId).forEach(i -> listOfItems.add(findItemById(i)));
        return listOfItems;
    }

}
