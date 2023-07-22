package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestEntityManager manager;

    User requester;

    ItemRequest itemRequest;

    Sort sort = Sort.by(Sort.Direction.DESC, "created");

    PageRequest page = PageRequest.of(0, 1, sort);

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .id(1L)
                .name("Java")
                .email("java@yandex.ru")
                .build();
        userRepository.save(requester);

        itemRequest = new ItemRequest(1L, "description",
                requester, LocalDateTime.now());

        itemRequestRepository.save(itemRequest);
    }



    @Test
    void findByRequesterId_thenReturnItemRequest() {
        Long requesterId = 1L;
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId(requesterId);
        assertEquals(1, itemRequests.size());
    }

    @Test
    void findAllByRequesterIdNot_thenReturnItemRequest() {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdNot(5L, page);
        assertEquals(1, itemRequests.size());
    }
}