package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User getById(int id);

    List<User> getAll();

    void delete(int id);

    void removeFriend(int userId, int friendId);

    RowMapper<User> getUserRowMapper();

}
