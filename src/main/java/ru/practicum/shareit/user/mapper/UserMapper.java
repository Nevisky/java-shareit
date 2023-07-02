package ru.practicum.shareit.user.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserMapper {

    public static UserDto toUserDto(User newUser) {
        return UserDto.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .build();
    }

    public static User toUser(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static Collection<UserDto> listToUserDto(Collection<User> users) {
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

}
