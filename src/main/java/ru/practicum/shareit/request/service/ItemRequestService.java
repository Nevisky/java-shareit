package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestWithItems;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestWithItems getRequestById(Long userId, Long requestId);

    ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long userId);

   List<ItemRequestWithItems> getItemsRequestByRequestorId(Long userId);

    List<ItemRequestWithItems> getAllRequestsByPageable(Long userId, int from, int size);

}