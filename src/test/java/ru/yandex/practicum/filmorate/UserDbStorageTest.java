package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);

        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("testlogin");
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldlogin");
        user.setName("Old User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        createdUser.setEmail("new@example.com");
        createdUser.setName("Updated User");
        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(updatedUser.getName()).isEqualTo("Updated User");
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        userStorage.delete(createdUser.getId());

        List<User> users = userStorage.getAll();
        assertThat(users).isEmpty();
    }

    @Test
    void testRemoveFriend() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)",
                createdUser1.getId(), createdUser2.getId());
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)",
                createdUser2.getId(), createdUser1.getId());

        userStorage.removeFriend(createdUser1.getId(), createdUser2.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, createdUser1.getId(), createdUser2.getId());
        assertThat(count).isZero();
    }
}



