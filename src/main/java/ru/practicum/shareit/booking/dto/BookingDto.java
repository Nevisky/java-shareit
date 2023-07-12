package ru.practicum.shareit.booking.dto;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.util.BookingStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {

    Long id;

    @NotNull
    LocalDateTime start;

    @NotNull
    LocalDateTime end;

    Long itemId;

    Long bookerId;

    BookingStatus status;

}