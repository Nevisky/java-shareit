package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;


@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Создан запрос на предмет от пользователя user_id = {}", userId);
        return itemRequestService.createRequest(itemRequestDto,userId);
    }

    @GetMapping
    public List<ItemRequestWithItems> getItemRequestResponseByRequestorId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Создан запрос на получение всех предметов от пользователя user_id = {}", userId);
        return itemRequestService.getItemsRequestByRequestorId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItems> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "0",required = false)
                                                     @Min(0) int from,
                                                     @RequestParam(defaultValue = "20",required = false)
                                                     @Positive int size) {
        log.info("Создан запрос на получение всех предметов");
        return itemRequestService.getAllRequestsByPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItems getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable Long requestId) {
        log.info("Создан запрос на получение предмета по id = {}", requestId);
        return itemRequestService.getRequestById(userId,requestId);
    }

}