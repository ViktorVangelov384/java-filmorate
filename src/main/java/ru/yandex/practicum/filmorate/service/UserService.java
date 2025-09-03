package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        validateName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        try {
            userStorage.getById(user.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateName(user);
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        try {
            return userStorage.getById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    public void addFriend(int userId, int friendId) {
        try {
            userStorage.getById(userId);
            userStorage.getById(friendId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        ((UserDbStorage) userStorage).addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        try {
            userStorage.getById(userId);
            userStorage.getById(friendId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        try {
            userStorage.getById(userId);
            userStorage.getById(otherId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        return ((UserDbStorage) userStorage).getCommonFriends(userId, otherId);
    }

    public List<User> getFriends(int userId) {
        userStorage.getById(userId);
        return userStorage.getFriends(userId);
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

