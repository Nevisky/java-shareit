package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    Long id;

    @NotBlank(message = "Добавьте описание")
    String description;

    Long requestor;

    LocalDateTime created;

}