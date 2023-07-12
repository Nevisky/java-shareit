package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;

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

    @GetMapping("/{itemId}")
    public ItemDto findItemById(@PathVariable("itemId") Long itemId,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Найден Item itemId={}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam("text") String text) {
        log.info("Поиск по тексту text={}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemService.search(text);
    }

    @GetMapping
    public Collection<ItemDto> findAllItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Найдены все Items пользователя userId={}", userId);
        return itemService.getAllItemsByUser(userId);
    }

}
