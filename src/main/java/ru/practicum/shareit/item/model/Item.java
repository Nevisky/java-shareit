package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {

    @Positive
    Long id;

    @NotBlank(message = "Parameter name is empty")
    String name;

    @NotBlank
    @Size(max = 200, message = "length of description is more then 200 symbols")
    String description;

    @NotNull(message = "Parameter name is NULL")
    Boolean available;

    @NotNull
    User owner;

    ItemRequest request;

}
