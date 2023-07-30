package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;


@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Создан запрос на предмет от пользователя user_id = {}", userId);
        return itemRequestClient.addRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestResponseByRequestorId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Создан запрос на получение всех предметов от пользователя user_id = {}", userId);
        return itemRequestClient.getItemsRequestByRequesterId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "0")
                                                     @Min(0) Integer from,
                                                     @RequestParam(defaultValue = "10")
                                                     @Positive Integer size) {
        log.info("Создан запрос на получение всех предметов");
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable Long requestId) {
        log.info("Создан запрос на получение предмета по id = {}", requestId);
        return itemRequestClient.getRequestById(userId,requestId);
    }

}