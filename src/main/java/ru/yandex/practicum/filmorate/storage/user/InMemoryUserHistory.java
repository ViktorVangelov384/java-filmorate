package ru.yandex.practicum.filmorate.storage.user;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.*;

@Component
public class InMemoryUserHistory implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;

    }

    @Override
    public User getById(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    @Override
    public Set<Integer> getFriends(int userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return user.getFriends().keySet();
    }

    @Override
    public void addFriend(int userId, int friendId, FriendshipStatus status) {
        User user = getById(userId);
        User friend = getById(friendId);
        FriendshipStatus reverseStatus = friend.getFriends().get(userId);
        if (reverseStatus == FriendshipStatus.PENDING && status == FriendshipStatus.PENDING) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
        } else {
            user.getFriends().put(friendId, FriendshipStatus.PENDING);
            friend.getFriends().put(userId, FriendshipStatus.PENDING);
        }
        user.getFriends().put(friendId, status);
        friend.getFriends().put(userId, status);
    }

    @Override
    public void updateFriendStatus(int userId, int friendId, FriendshipStatus status) {
        User user = getById(userId);
        user.getFriends().put(friendId, status);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public FriendshipStatus getFriendshipStatus(int userId, int friendId) {
        User user = getById(userId);
        return user.getFriends().getOrDefault(friendId, null);
    }

}
