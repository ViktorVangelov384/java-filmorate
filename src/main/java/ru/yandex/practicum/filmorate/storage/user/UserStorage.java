package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.List;
import java.util.Set;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User getById(int id);

    List<User> getAll();

    void delete(int id);

    Set<Integer> getFriends(int userId);

    void addFriend(int userId, int friendId, FriendshipStatus status);

    void updateFriendStatus(int userId, int friendId, FriendshipStatus status);

    void removeFriend(int userId, int friendId);

    FriendshipStatus getFriendshipStatus(int userId, int friendId);

}
