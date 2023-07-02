package ru.practicum.shareit.item.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static ru.practicum.shareit.item.mapper.ItemMapper.*;

/**
 * TODO Sprint add-controllers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    @Override
    public ItemDto saveItem(Long userId, ItemDto itemDto) {
        if(userRepository.findUserById(userId) == null){
            throw new ObjectNotFoundException("Запрашиваемого пользователя не существует");
        }
        return toItemDto(itemRepository.save(userId, ItemMapper.toItem(itemDto,userRepository.findUserById(userId))));
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) {
        Item excistedItem = itemRepository.findItemById(itemId);
        if (excistedItem == null) {
            throw new ObjectNotFoundException("Нет пользоваьеля с ID: " + itemId);
        }
        if(!itemRepository.getUsersItems().containsKey(userId)){
            throw new ObjectNotFoundException("Данная вещь не принадлежит пользователю");
        }
        Item item = Item.builder()
                .id(itemId)
                .name(itemDto.getName() != null ? itemDto.getName() : excistedItem.getName())
                .description(itemDto.getDescription() != null
                        ? itemDto.getDescription() : excistedItem.getDescription())
                .available(itemDto.getAvailable() != null
                        ? itemDto.getAvailable() : excistedItem.getAvailable())
                .build();
        return ItemMapper.toItemDto(itemRepository.update(userId,item));
    }

    @Override
    public ItemDto getItemById(Long userId,Long itemId) {
        return toItemDto(itemRepository.findItemById(itemId));
    }

    @Override
    public Collection<ItemDto> search(String text) {
        return listToItemDto(itemRepository.findItemFromText(text));
    }

    @Override
    public Collection<ItemDto> getAllItemsByUser(Long userId) {
        return listToItemDto(itemRepository.findAllItemsByUser(userId));
    }

}
