package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User getById(int id);

    List<User> getAll();

    void delete(int id);

    Set<Integer> getFriends(int userId);
}
