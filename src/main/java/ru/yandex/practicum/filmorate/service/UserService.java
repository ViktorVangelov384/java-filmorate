package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        validateName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (userStorage.getById(user.getId()) == null) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateName(user);
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        FriendshipStatus existingStatus = userStorage.getFriendshipStatus(friendId, userId);

        if (existingStatus == FriendshipStatus.PENDING) {
            userStorage.updateFriendStatus(userId, friendId, FriendshipStatus.CONFIRMED);
            userStorage.updateFriendStatus(friendId, userId, FriendshipStatus.CONFIRMED);
        } else if (existingStatus == null) {
            userStorage.addFriend(userId, friendId, FriendshipStatus.PENDING);
            userStorage.addFriend(friendId, userId, FriendshipStatus.PENDING);
        }
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (!user.getFriends().containsKey(friendId)) {
            throw new NotFoundException("Друг с id " + friendId + " не найден у пользователя " + userId);
        }
        userStorage.removeFriend(userId, friendId);
        userStorage.removeFriend(friendId, userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        User otherUser = userStorage.getById(otherId);
        if (otherUser == null) {
            throw new NotFoundException("Пользователь с id " + otherId + " не найден");
        }

        Set<Integer> userFriends = user.getFriends().keySet();
        Set<Integer> otherFriends = otherUser.getFriends().keySet();
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        return userStorage.getFriends(userId).stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());

    }

    public List<User> getFriendRequests(int userId) {
        User user = userStorage.getById(userId);
        List<User> result = user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.PENDING)
                .map(entry -> userStorage.getById(entry.getKey()))
                .collect(Collectors.toList());
        return result;
    }

    public void confirmFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        FriendshipStatus status = userStorage.getFriendshipStatus(userId, friendId);
        if (status == null || status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Нет pending запроса на дружбу от пользователя " + friendId);
        }

        userStorage.updateFriendStatus(userId, friendId, FriendshipStatus.CONFIRMED);
        userStorage.updateFriendStatus(friendId, userId, FriendshipStatus.CONFIRMED);

        user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
    }

    public List<User> getConfirmedFriends(int userId) {
        User user = userStorage.getById(userId);
        return user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(entry -> userStorage.getById(entry.getKey()))
                .collect(Collectors.toList());
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
