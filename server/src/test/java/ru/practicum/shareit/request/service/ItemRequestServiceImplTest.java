package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItems;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @InjectMocks
    ItemRequestServiceImpl requestService;
    User requestor;
    User owner;
    Item item;
    ItemRequest request;
    Sort sort = Sort.by(Sort.Direction.DESC, "created");
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Name");
        owner.setEmail("email@email.com");
        owner.setId(1L);

        requestor = new User();
        requestor.setName("Name2");
        requestor.setEmail("email2@email.com");
        requestor.setId(2L);

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        request.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
    }

    @Test
    void saveRequest_thenReturnItemRequestDto() {
        long userId = requestor.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any())).thenReturn(request);

        ItemRequestDto requestDto = requestService.createRequest(
                ItemRequestDto.builder().description("description").build(), requestor.getId());

        assertNotNull(requestDto);
        assertEquals(request.getId(), requestDto.getId());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void saveRequest_whenUserNotFound_thenReturnObjectNotFoundException() {
        long userIdNotFound = 999L;
        String error = String.format("Пользователь с id = %d не найден", userIdNotFound);
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> requestService.createRequest(
                        ItemRequestDto.builder().description("description").build(), userIdNotFound)
        );
        assertEquals(error, exception.getMessage());
        verify(itemRequestRepository, times(0)).save(any());
    }

    @Test
    void getAllRequestsWithRequester_thenReturnListItemsRequest() {
        long userId = requestor.getId();
        when(itemRequestRepository.findByRequestorId(userId)).thenReturn(List.of(request));
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));

        List<ItemRequestWithItems> requests = requestService.getItemsRequestByRequestorId(userId);

        assertNotNull(requests);
        assertEquals(1, requests.size());
        verify(itemRequestRepository, times(1)).findByRequestorId(userId);
    }

    @Test
    void getRequestWithRequesterById_thenReturnItemRequest() {
        long userId = requestor.getId();
        long requestId = request.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestWithItems requestDto = requestService.getRequestById(userId, requestId);

        assertNotNull(requestDto);
        assertEquals(requestId, requestDto.getId());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(item.getId(), requestDto.getId());
    }

    @Test
    void getRequests_thenReturnListItemRequest() {
        long userId = owner.getId();
        PageRequest page = PageRequest.of(0, 1, sort);
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdNot(userId, page)).thenReturn(List.of(request));
        List<ItemRequestWithItems> requestDtos = requestService.getAllRequestsByPageable(userId, 0, 1);
        assertNotNull(requestDtos);
        assertEquals(1, requestDtos.size());
    }

    @Test
    void getRequests_thenReturnEmptyList() {
        long userId = requestor.getId();
        PageRequest page = PageRequest.of(0, 1, sort);
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdNot(userId, page)).thenReturn(Collections.emptyList());
        List<ItemRequestWithItems> requestDtos = requestService.getAllRequestsByPageable(userId, 0, 1);
        assertNotNull(requestDtos);
        assertEquals(0, requestDtos.size());
    }

}