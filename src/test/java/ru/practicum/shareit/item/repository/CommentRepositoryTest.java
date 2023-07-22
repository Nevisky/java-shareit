package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;
    Item item;

    User owner;

    User author;

    Comment comment;

    @BeforeEach
    void setUp() {

        author = User.builder()
                .email("author@mail.ru")
                .name("Owner")
                .build();
        userRepository.save(author);

        owner = User.builder()
                .email("owner@mail.ru")
                .name("Owner")
                .build();
        userRepository.save(owner);

        item = Item.builder()
                .name("Equipment")
                .owner(owner)
                .available(true)
                .description("description")
                .build();
        itemRepository.save(item);

        comment = Comment.builder()
                .text("Equipment")
                .item(item)
                .author(author)
                .createdDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

    }

    @Test
    void findByItemId() {
        List<Comment> comments = commentRepository.findByItemId(comment.getItem().getId());
        assertEquals(1, comments.size());
    }

    @Test
    void findByItemIdIn() {
        List<Comment> comments = commentRepository.findByItemIdIn(List.of(item.getId()));
        assertEquals(1, comments.size());
    }
}