package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserService userService;
    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Создан пользователь user={}", userDto);
        return userService.saveUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@Valid @RequestBody UserDto userDto,
                           @PathVariable("userId") Long userId){
        log.info("Обновлен пользователь userId={}", userId);
        return userService.updateUser(userId,userDto);
    }
    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable("userId") Long userId) {
        log.info("Найден пользователь userId={}", userId);
        return userService.findUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public UserDto delete(@PathVariable("userId") Long userId) {
        log.info("Удален пользователь userId={}", userId);
        return userService.deleteUser(userId);
    }

    @GetMapping
    public Collection<UserDto> findAllUsers() {
        log.info("Найдено пользователей userId={}",userService.findAllUsers().size());
        return userService.findAllUsers();
    }

}
