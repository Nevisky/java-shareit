package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Autowired
    EntityManager entityManager;
    Item item;

    User owner;

    User author;

    Comment comment;

    ItemRequest itemRequest;

    User requester;

    Sort sort = Sort.by(Sort.Direction.DESC, "id");

    PageRequest page = PageRequest.of(0, 1, sort);

    @BeforeEach
    void setUp() {

        requester = User.builder()
                .email("requester@mail.ru")
                .name("Owner")
                .build();
        userRepository.save(requester);

        owner = User.builder()
                .email("owner@mail.ru")
                .name("Owner")
                .build();
        userRepository.save(owner);

        itemRequest = ItemRequest.builder()
                .created(LocalDateTime.now())
                .description("description")
                .requestor(requester)
                .build();
        itemRequest = itemRequestRepository.save(itemRequest);

        item = Item.builder()
                .name("Equipment")
                .owner(owner)
                .available(true)
                .request(itemRequest)
                .description("description")
                .build();
        itemRepository.save(item);

        comment = Comment.builder()
                .text("Equipment")
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
    }


    @Test
    void findByRequestId() {
        List<Item> itemList = itemRepository.findByRequestId(itemRequest.getId());
        assertNotNull(itemList);
        assertEquals(1, itemList.size());
        assertEquals(itemRequest.getId(), itemList.get(0).getRequest().getId());
    }

    @Test
    void search() {
        List<Item> itemList = itemRepository.search("des", page);
        assertEquals(1, itemList.size());
        assertEquals(item, itemList.get(0));
    }

    @Test
    void findItemByOwnerId() {
        List<Item> itemList = itemRepository.findItemByOwnerId(owner.getId(), page);
        assertEquals(1, itemList.size());
        assertEquals(item, itemList.get(0));
    }

    @Test
    void findByRequestIdIn() {
        List<Item> itemList = itemRepository.findByRequestIdIn(List.of(itemRequest.getId()));
        assertEquals(1, itemList.size());
        assertEquals(item.getId(), itemList.get(0).getId());
    }


}