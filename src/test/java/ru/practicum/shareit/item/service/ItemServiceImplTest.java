package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserService userService;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(120);
        LocalDateTime end = LocalDateTime.now().minusSeconds(60);
        owner = User.builder()
                .id(1L)
                .name("Test")
                .email("Test@email.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Test2")
                .email("Test@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("TestItem")
                .description("TestDescription")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("TestText")
                .author(booker)
                .item(item)
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void saveItem_thenReturnSavedItemDto() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto saveItemDto = ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        ItemDto itemDto = itemService.saveItem(userId, saveItemDto);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItem_thenReturnItemDto() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        String newName = "nameUpdate";
        String newDescription = "newDescription";
        item.setName(newName);
        item.setDescription(newDescription);
        when(itemRepository.save(any())).thenReturn(item);
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .name(newName)
                .description(newDescription)
                .build();
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        ItemDto itemDto = itemService.updateItem(userId, itemDtoToUpdate, itemId);
        assertNotNull(itemDto);
        assertEquals("nameUpdate", itemDto.getName());
    }

    @Test
    void createComment_thenReturnCommentDto() {
        long userId = booker.getId();
        long itemId = item.getId();
        UserDto bookerDto = UserMapper.toUserDto(booker);
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findTopByStatusNotLikeAndItemIdAndBookerIdOrderByEndAsc(BookingStatus.REJECTED, itemId, bookerDto.getId()))
                .thenReturn(booking);
        when(commentRepository.save(any())).thenReturn(comment);
        CommentDto commentDto = CommentDto.builder().text("text").build();

        CommentDto commentDtoOut = itemService.createComment(commentDto, userId, itemId);

        assertNotNull(commentDtoOut);
        assertEquals(comment.getId(), commentDtoOut.getId());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void getAllItemsByUserId_thenReturnEmptyList() {
        UserDto userDto = UserMapper.toUserDto(booker);
        userService.saveUser(userDto);
        long userId = booker.getId();
        when(userRepository.findById(booker.getId())).thenReturn(Optional.ofNullable(booker));
        when(itemRepository.findItemByOwnerId(any(), any())).thenReturn(Collections.emptyList());

        List<ItemDtoWithBookingAndComments> itemDto = itemService.getItemDtoByUserId(userId, 0, 1);

        assertNotNull(itemDto);
        assertEquals(0, itemDto.size());
    }


    @Test
    void getAllItemsByUserId_thenReturnListItems() {
        long userId = owner.getId();
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("id"));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findItemByOwnerId(userId, pageRequest)).thenReturn(List.of(item));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(userId)).thenReturn(List.of(booking));

        List<ItemDtoWithBookingAndComments> itemOwnerDto = itemService.getItemDtoByUserId(userId, 0, 1);

        assertNotNull(itemOwnerDto);
        assertEquals(1, itemOwnerDto.size());
    }

    @Test
    void getItemById_thenReturnItem() {
        long ownerId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(ownerId)).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(itemId)).thenReturn(List.of(booking));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemDtoWithBookingAndComments itemOwnerDto = itemService.getItemById(ownerId, itemId);

        assertNotNull(itemOwnerDto);
        assertEquals(itemId, itemOwnerDto.getId());
        assertEquals(comment.getId(), itemOwnerDto.getComments().get(0).getId());
    }

    @Test
    void getSearchItem_thenReturnListItems() {
        when(itemRepository.search(any(), any())).thenReturn(List.of(item));

        Collection<ItemDto> itemDto = itemService.getItemsDtoByRequest("NameItem", 0, 1);

        assertNotNull(itemDto);
        assertEquals(1, itemDto.size());
        assertEquals(item.getId(), new ArrayList<>(itemDto).get(0).getId());
    }

    @Test
    void getSearchItem_thenReturnEmptyList() {
        Collection<ItemDto> listItemDto = itemService.getItemsDtoByRequest("", 0, 1);

        assertNotNull(listItemDto);
        assertEquals(0, listItemDto.size());
    }

}