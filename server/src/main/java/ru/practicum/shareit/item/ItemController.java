package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingAndComments;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Создан новый Item itemId={}", itemDto.getId());
        return itemService.saveItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable("itemId") Long itemId,
                          @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Обновлен Item itemId={}", itemDto.getId());
        return itemService.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("{itemId}")
    public ItemDtoWithBookingAndComments getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long itemId) {
        log.info("Найден предмет по id = {}", itemId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoWithBookingAndComments> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @RequestParam(name = "from", defaultValue = "0")
                                                                @PositiveOrZero int from,
                                                                @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.info("Найдены все Items пользователя userId={}", userId);
        return itemService.getItemDtoByUserId(userId, from, size);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable Long itemId) {
        log.info("Запрос на создание комментария");
        return itemService.createComment(commentDto, userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getItemsByTextRequest(@RequestParam String text,
                                                     @RequestParam(name = "from", defaultValue = "0")
                                                     @PositiveOrZero int from,
                                                     @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.info("Найден предмет по тексту запроса");
        return itemService.getItemsDtoByRequest(text, from, size);
    }

}