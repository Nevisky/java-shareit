package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking_example.dto.BookingDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoWithBookingAndComments {

    Long id;

    String name;

    String description;

    Boolean available;

    Long requestId;

    BookingDto lastBooking;

    BookingDto nextBooking;

    List<CommentDto> comments;

}