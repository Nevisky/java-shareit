package ru.practicum.shareit.booking.dto;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.util.BookingStatus;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {

    Long id;
    @FutureOrPresent
    @NotNull
    LocalDateTime start;

    @NotNull
    @Future
    LocalDateTime end;

    @NotNull
    Long itemId;

    Long bookerId;

    BookingStatus status;

}