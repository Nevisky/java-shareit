package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    ItemRequestRepository itemRequestRepository;
    @Autowired
    UserRepository userRepository;
    ItemRequest itemRequest;
    User requester;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .name("Java")
                .email("java@yandex.ru")
                .build();

        itemRequest = ItemRequest.builder()
                .created(LocalDateTime.now().plusSeconds(2))
                .description("desc")
                .requestor(requester)
                .build();
    }

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }

    @Test
    void verifyBootByPersistRequest() {
        assertNull(itemRequest.getId());
        entityManager.persist(requester);
        entityManager.persist(itemRequest);
        assertNotNull(itemRequest.getId());
    }

    @Test
    void verifyRepositoryByPersistRequest() {
        assertNull(itemRequest.getId());
        userRepository.save(requester);
        itemRequestRepository.save(itemRequest);
        assertNotNull(itemRequest.getId());
    }

    @Test
    void findByRequestorId_thenReturnEmptyList() {
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(1L);
        assertNotNull(requests);
        assertEquals(0, requests.size());
    }

    @Test
    void findByRequestorId_thrnReturnListItemRequestor() {
        entityManager.persist(requester);
        entityManager.persist(itemRequest);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(requester.getId());
        assertNotNull(requests);
        assertEquals(1, requests.size());
    }

    @Test
    void findByRequesterIdNotWithPaging_thenReturnEmptyList() {
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(1L);
        assertNotNull(requests);
        assertEquals(0, requests.size());
    }

}