package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Создан новый Item itemId={}", itemDto.getId());
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable("itemId") Long itemId,
                          @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Обновлен Item itemId={}", itemDto.getId());
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long itemId) {
        log.info("Найден предмет по id = {}", itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @RequestParam(name = "from", defaultValue = "0")
                                                                @PositiveOrZero int from,
                                                                @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.info("Найдены все Items пользователя userId={}", userId);
        return itemClient.getItemByUserId(userId, from, size);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable Long itemId) {
        log.info("Запрос на создание комментария");
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByTextRequest(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                        @RequestParam String text,
                                                     @RequestParam(name = "from", defaultValue = "0")
                                                     @PositiveOrZero int from,
                                                     @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.info("Найден предмет по тексту запроса");
        return itemClient.search(userId, text, from, size);
    }

}