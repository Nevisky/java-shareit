package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestWithItems {

    Long id;

    @NotBlank(message = "Пользователя не существует")
    String description;

    @NotBlank(message = "Описание не может быть пустым")
    User requestor;

    LocalDateTime created;

    Collection<ItemDto> items;

}