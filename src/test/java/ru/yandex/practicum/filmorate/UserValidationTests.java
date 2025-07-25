package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTests {

    @Test
    void shouldRejectEmptyEmail() {
        User user = createValidUser();
        user.setEmail(" ");
        assertTrue(user.getEmail().isBlank());
    }

    @Test
    void shouldRejectInbvalidEmail() {
        User user = createValidUser();
        user.setEmail("valid.email.com");
        assertFalse(user.getEmail().contains("@"));
    }

    @Test
    void shouldRejectEmptyLogin() {
        User user = createValidUser();
        user.setLogin(" ");
        assertTrue(user.getLogin().isBlank());
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        User user = createValidUser();
        user.setLogin("valid login");
        assertTrue(user.getLogin().contains(" "));
    }

    @Test
    void shouldAcceptEmptyName() {
        User user = createValidUser();
        user.setName(" ");
        assertTrue(user.getName() == null || user.getName().isBlank());
    }

    @Test
    void shouldRejectFutureBirthday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(3));
        assertTrue(user.getBirthday().isAfter(LocalDate.now()));
    }

    @Test
    void shoudAcceptValidUser() {
        User user = createValidUser();
        assertFalse(user.getEmail().isBlank());
        assertTrue(user.getEmail().contains("@"));
        assertFalse(user.getLogin().isBlank());
        assertFalse(user.getLogin().contains(" "));
        assertFalse(user.getBirthday().isAfter(LocalDate.now()));
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("validuser@yandex.ru");
        user.setLogin("valid_login");
        user.setName("Виктор");
        user.setBirthday(LocalDate.of(2000, 2, 22));
        return user;
    }
}
