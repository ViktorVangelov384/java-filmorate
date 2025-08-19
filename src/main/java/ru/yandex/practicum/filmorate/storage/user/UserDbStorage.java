package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        updateFriends(user);

        return getById(user.getId());
    }

    @Override
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE user_id =?";
        User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);

        if (user != null) {
            loadFriends(user);
        }

        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        users.forEach(this::loadFriends);

        return users;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Set<Integer> getFriends(int userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getInt("friend_id"), userId));
    }

    @Override
    public void addFriend(int userId, int friendId, FriendshipStatus status) {
        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == null || count == 0) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES(?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, friendId, status.toString());
        } else {
            String updateSql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(updateSql, status.toString(), userId, friendId);
        }
    }

    @Override
    public void updateFriendStatus(int userId, int friendId, FriendshipStatus status) {
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, status.toString(), userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == null || count == 0) {
            throw new NotFoundException("Друг с id " + friendId + " не найден у пользователя " + userId);
        }
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public FriendshipStatus getFriendshipStatus(int userId, int friendId) {
        String sql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            String status = jdbcTemplate.queryForObject(sql, String.class, userId, friendId);
            return FriendshipStatus.valueOf(status);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            int friendId = rs.getInt("friend_id");
            FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
            user.getFriends().put(friendId, status);
            return null;
        }, user.getId());
    }

    private void updateFriends(User user) {
        user.getFriends().forEach((friendId, status) -> {
            String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getId(), friendId);

            if (count > 0) {
                String updateSql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
                jdbcTemplate.update(updateSql, status.toString(), user.getId(), friendId);
            } else {
                String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES(?, ?, ?)";
                jdbcTemplate.update(insertSql, user.getId(), friendId, status.toString());
            }
        });
    }
}

