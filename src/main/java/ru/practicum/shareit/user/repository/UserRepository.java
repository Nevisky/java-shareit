package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    User addUser(User user);

    User updateUser(Long userID,User user);

    User findUserById(Long id);

    User removeUser(Long id);

    Collection<User> getAllUsers();

}
