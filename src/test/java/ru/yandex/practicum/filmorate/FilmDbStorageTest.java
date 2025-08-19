package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@ActiveProfiles("test")
class FilmDbStorageTest extends AbstractStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void testCreateFilm() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isPositive();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.create(createTestFilm());
        film.setName("Updated Film");

        Film updatedFilm = filmStorage.update(film);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    void testGetById() {
        Film film = filmStorage.create(createTestFilm());
        Film foundFilm = filmStorage.getById(film.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetAllFilms() {
        filmStorage.create(createTestFilm());
        filmStorage.create(createTestFilm2());

        List<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void testAddLike() {
        Film film = filmStorage.create(createTestFilm());
        ru.yandex.practicum.filmorate.model.User user = createTestUser();
        userStorage.create(user);

        film.getLikes().add(user.getId());
        filmStorage.update(film);

        Film filmWithLike = filmStorage.getById(film.getId());
        assertThat(filmWithLike.getLikes()).contains(user.getId());
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(1);
        genres.add(genre);
        film.setGenres(genres);

        return film;
    }

    private Film createTestFilm2() {
        Film film = new Film();
        film.setName("Test Film 2");
        film.setDescription("Test Description 2");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(90);

        Mpa mpa = new Mpa();
        mpa.setId(2);
        film.setMpa(mpa);

        return film;
    }

    private ru.yandex.practicum.filmorate.model.User createTestUser() {
        ru.yandex.practicum.filmorate.model.User user = new ru.yandex.practicum.filmorate.model.User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}