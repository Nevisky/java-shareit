package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailAlreadyExistException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;


import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository{

    private long id = 0;

    private static final Map<Long, User> users = new HashMap<>();

    private static final Map<Long,String> emails = new HashMap<>();

    @Override
    public User addUser(User user) {
        if(emails.containsValue(user.getEmail())){
            throw new EmailAlreadyExistException((String.format("Пользователь с данным e-mail = %s существует, введите другую почту.", user.getEmail())));
        }
        if(user.getEmail() == null){
            throw new ValidationException("Поле e-mail пустое");
        }
        user.setId(++id);
        users.put(user.getId(),user);
        emails.put(user.getId(), user.getEmail());
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        User newUser = users.get(userId);
        if (!users.containsKey(userId)) {
            throw new ValidationException((String.format("Пользователь с таким id = %d не найден", userId)));
        }
        if (emails.containsValue(user.getEmail())) {
            if (!Objects.equals(newUser.getEmail(), user.getEmail())) {
                throw new EmailAlreadyExistException(String.format("Пользователь с данным e-mail = %s существует, введите другую почту.", user.getEmail()));
            }
        }
        if (user.getName() != null) newUser.setName(user.getName());
        if (user.getEmail() != null) {
            newUser.setEmail(user.getEmail());
            emails.put(userId, user.getEmail());
        }
        return newUser;
    }

    @Override
    public User findUserById(Long userid) {
        return users.get(userid);
    }

    @Override
    public User removeUser(Long userId) {
        User deleteUser = findUserById(userId);
        users.remove(userId);
        emails.remove(userId);
        return deleteUser;
    }

    @Override
    public Collection<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

}
