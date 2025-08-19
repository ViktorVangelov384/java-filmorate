package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest extends AbstractStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void testCreateUser() {
        User user = createTestUser();
        User createdUser = userStorage.create(user);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.create(createTestUser());
        user.setName("Updated Name");

        User updatedUser;
        updatedUser = userStorage.update(user);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testGetById() {
        User user = userStorage.create(createTestUser());
        User foundUser = userStorage.getById(user.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testGetAllUsers() {
        userStorage.create(createTestUser());
        userStorage.create(createTestUser2());

        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void testAddFriend() {
        User user1 = userStorage.create(createTestUser());
        User user2 = userStorage.create(createTestUser2());

        userStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);

        User userWithFriends = userStorage.getById(user1.getId());
        assertThat(userWithFriends.getFriends()).containsKey(user2.getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = userStorage.create(createTestUser());
        User user2 = userStorage.create(createTestUser2());

        userStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);
        userStorage.removeFriend(user1.getId(), user2.getId());

        User userWithoutFriend = userStorage.getById(user1.getId());
        assertThat(userWithoutFriend.getFriends()).doesNotContainKey(user2.getId());
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private User createTestUser2() {
        User user = new User();
        user.setEmail("test2@mail.ru");
        user.setLogin("testLogin2");
        user.setName("Test User 2");
        user.setBirthday(LocalDate.of(1995, 1, 1));
        return user;
    }
}
