package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmDbStorage;

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void testCreateFilm() {
        Film testFilm = createTestFilm();

        Film createdFilm = filmDbStorage.create(testFilm);

        assertNotNull(createdFilm.getId());
        assertTrue(createdFilm.getId() > 0);

        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());

        assertNotNull(createdFilm.getMpa());
        assertEquals(1, createdFilm.getMpa().getId());
        assertEquals("G", createdFilm.getMpa().getName());

        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size());
        assertTrue(createdFilm.getGenres().stream().anyMatch(
                g -> g.getId() == 1 && g.getName().equals("Комедия")));
        assertTrue(createdFilm.getGenres().stream().anyMatch(
                g -> g.getId() == 2 && g.getName().equals("Драма")));

        Integer filmsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE film_id = ?", Integer.class, createdFilm.getId());
        assertEquals(1, filmsCount);

        Integer mpaId = jdbcTemplate.queryForObject(
                "SELECT mpa_id FROM films WHERE film_id = ?", Integer.class, createdFilm.getId());
        assertEquals(1, mpaId);

        Integer genresCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_genres WHERE film_id = ?", Integer.class, createdFilm.getId());
        assertEquals(2, genresCount);

    }

    @Test
    void testGetFilmById() {
        Film testFilm = createTestFilm();
        Film createdFilm = filmDbStorage.create(testFilm);

        Film retrievedFilm = filmDbStorage.getById(createdFilm.getId());

        assertNotNull(retrievedFilm);
        assertEquals(createdFilm.getId(), retrievedFilm.getId());
        assertEquals("Test Film", retrievedFilm.getName());
        assertEquals("Test Description", retrievedFilm.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), retrievedFilm.getReleaseDate());
        assertEquals(120, retrievedFilm.getDuration());
        assertEquals(1, retrievedFilm.getMpa().getId());
        assertEquals("G", retrievedFilm.getMpa().getName());
        assertEquals(2, retrievedFilm.getGenres().size());
    }

    @Test
    void testUpdateFilm() {
        Film testFilm = createTestFilm();
        Film createdFilm = filmDbStorage.create(testFilm);

        createdFilm.setName("Updated Film Name");
        createdFilm.setDescription("Updated Description");
        createdFilm.setDuration(150);
        createdFilm.setMpa(new Mpa(2, "PG"));

        Set<Genre> updatedGenres = new TreeSet<>((g1, g2) -> Integer.compare(g1.getId(), g2.getId()));
        updatedGenres.add(new Genre(3, "Мультфильм"));
        createdFilm.setGenres(updatedGenres);

        Film updatedFilm = filmDbStorage.update(createdFilm);

        assertEquals(createdFilm.getId(), updatedFilm.getId());
        assertEquals("Updated Film Name", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());
        assertEquals(2, updatedFilm.getMpa().getId());
        assertEquals("PG", updatedFilm.getMpa().getName());
        assertEquals(1, updatedFilm.getGenres().size());
        assertTrue(updatedFilm.getGenres().stream().anyMatch(g -> g.getId() == 3));
    }

    @Test
    void testGetAllFilms() {
        Film film1 = createTestFilm();
        Film createdFilm1 = filmDbStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Another Film");
        film2.setDescription("Another Description");
        film2.setReleaseDate(LocalDate.of(2021, 2, 2));
        film2.setDuration(90);
        film2.setMpa(new Mpa(2, "PG"));
        film2.setGenres(Set.of(new Genre(4, "Триллер")));

        Film createdFilm2 = filmDbStorage.create(film2);

        List<Film> allFilms = filmDbStorage.getAll();

        assertNotNull(allFilms);
        assertEquals(2, allFilms.size());
        assertTrue(allFilms.stream().anyMatch(f -> f.getId() == createdFilm1.getId()));
        assertTrue(allFilms.stream().anyMatch(f -> f.getId() == createdFilm2.getId()));
    }

    @Test
    void testDeleteFilm() {
        Film testFilm = createTestFilm();
        Film createdFilm = filmDbStorage.create(testFilm);

        assertNotNull(filmDbStorage.getById(createdFilm.getId()));

        filmDbStorage.delete(createdFilm.getId());

        List<Film> allFilms = filmDbStorage.getAll();
        assertTrue(allFilms.isEmpty());

        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
                () -> filmDbStorage.getById(createdFilm.getId()));
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = createTestFilm();
        Film createdFilm1 = filmDbStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Popular Film");
        film2.setDescription("Very popular film");
        film2.setReleaseDate(LocalDate.of(2022, 3, 3));
        film2.setDuration(110);
        film2.setMpa(new Mpa(3, "PG-13"));
        film2.setGenres(Set.of(new Genre(6, "Боевик")));

        Film createdFilm2 = filmDbStorage.create(film2);

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user1@mail.com", "user1", "User One",
                java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user2@mail.com", "user2", "User Two",
                java.sql.Date.valueOf(LocalDate.of(1991, 2, 2)));

        Integer user1Id = jdbcTemplate.queryForObject("SELECT user_id FROM users WHERE login = ?",
                Integer.class, "user1");
        Integer user2Id = jdbcTemplate.queryForObject("SELECT user_id FROM users WHERE login = ?",
                Integer.class, "user2");

        filmDbStorage.addLike(createdFilm2.getId(), user1Id);
        filmDbStorage.addLike(createdFilm2.getId(), user2Id);
        filmDbStorage.addLike(createdFilm1.getId(), user1Id);

        List<Film> popularFilms = filmDbStorage.getPopular(2);

        assertEquals(2, popularFilms.size());
        assertEquals(createdFilm2.getId(), popularFilms.get(0).getId());
        assertEquals(createdFilm1.getId(), popularFilms.get(1).getId());
    }

    @Test
    void testFilmWithSingleGenre() {
        Film film = new Film();
        film.setName("Single Genre Film");
        film.setDescription("Film with one genre");
        film.setReleaseDate(LocalDate.of(2023, 5, 5));
        film.setDuration(80);
        film.setMpa(new Mpa(5, "NC-17"));

        Set<Genre> genres = new TreeSet<>((g1, g2) -> Integer.compare(g1.getId(), g2.getId()));
        genres.add(new Genre(5, "Документальный"));
        film.setGenres(genres);

        Film createdFilm = filmDbStorage.create(film);
        Film retrievedFilm = filmDbStorage.getById(createdFilm.getId());

        assertNotNull(retrievedFilm);
        assertEquals("Single Genre Film", retrievedFilm.getName());
        assertEquals(1, retrievedFilm.getGenres().size());
        assertTrue(retrievedFilm.getGenres().stream().anyMatch(g -> g.getId() == 5));
        assertEquals("NC-17", retrievedFilm.getMpa().getName());
    }

    @Test
    void testGetNonExistentFilm() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
                () -> filmDbStorage.getById(9999));
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Set<Genre> genres = new TreeSet<>((g1, g2) -> Integer.compare(g1.getId(), g2.getId()));
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));
        film.setGenres(genres);

        return film;
    }
}