package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestWithItems;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User validateUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(String.format(
                "Пользователь с id = %d не найден", userId)));
    }

    private ItemRequest validateRequestItem(Long requestItemId) {
        return itemRequestRepository.findById(requestItemId).orElseThrow(() -> new ObjectNotFoundException(String.format(
                "Предмет с id = %d не найден", requestItemId)));
    }

    @Override
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long userId) {

        LocalDateTime now = LocalDateTime.now();
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto,validateUser(userId));
        itemRequest.setRequestor(validateUser(userId));
        itemRequest.setCreated(now);
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
}

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestWithItems> getItemsRequestByRequestorId(Long userId) {
        validateUser(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId(userId);
        return createItemsForRequest(itemRequests);
    }

    private List<ItemRequestWithItems> createItemsForRequest(List<ItemRequest> itemRequests) {
        Collection<Long> itemsId = itemRequests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<ItemDto> itemDtoList = itemRepository.findByRequestIdIn(itemsId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return itemRequests.stream()
                .map(ItemRequest -> ItemRequestMapper.toItemRequestWithItems(ItemRequest, itemDtoList))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestWithItems> getAllRequestsByPageable(Long userId, Integer from, Integer size) {
        validateUser(userId);
        Pageable page = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> itemsList = itemRequestRepository.findAllByRequestorIdNot(userId, page);
        if (itemsList.isEmpty()) {
            return Collections.emptyList();
        }
        return createItemsForRequest(itemsList);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestWithItems getRequestById(Long userId, Long requestId) {
        validateUser(userId);
        ItemRequest itemRequest = validateRequestItem(requestId);

        List<ItemDto> itemDtoList = itemRepository.findByRequestId(requestId)
                .stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestWithItems(itemRequest, itemDtoList);
    }

}