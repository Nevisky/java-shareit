package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItems;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(itemRequest.getId())
                .created(itemRequest.getCreated())
                .build();
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequest, User user) {
        return ItemRequest.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(user)
                .created(itemRequest.getCreated())
                .build();
    }

    public ItemRequestWithItems toItemRequestWithItems(ItemRequest itemRequest, Collection<ItemDto> itemDtoList) {
        return ItemRequestWithItems.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(itemRequest.getRequestor())
                .items(itemDtoList)
                .created(itemRequest.getCreated())
                .build();
    }

}