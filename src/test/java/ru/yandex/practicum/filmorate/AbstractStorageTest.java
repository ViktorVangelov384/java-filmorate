package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
public abstract class AbstractStorageTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }
}
