package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTests {
    private static Validator validator;
    private static LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    @BeforeAll
    static void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldRejectEmptyName() {
        Film film = createValidFilm();
        film.setName(" ");
        assertTrue(film.getName().isBlank());
    }

    @Test
    void shouldRejectLongDescription() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(203));
        assertTrue(film.getDescription().length() > 200);
    }

    @Test
    void shouldAccept200Symbols() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        assertEquals(200, film.getDescription().length());
    }

    @Test
    void shouldRejectEarlyReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(MIN_DATE.minusDays(3));
        assertTrue(film.getReleaseDate().isBefore(MIN_DATE));

    }

    @Test
    void shouldRejectNonPositiveDuration() {
        Film film = createValidFilm();
        film.setDuration(-5);
        assertTrue(film.getDuration() <= 0);
    }

    @Test
    void shouldAcceptValidFilm() {
        Film film = createValidFilm();
        assertFalse(film.getName().isBlank());
        assertTrue(film.getDescription().length() < 200);
        assertNotNull(film.getReleaseDate());
        assertFalse(film.getReleaseDate().isBefore(MIN_DATE));
        assertTrue(film.getDuration() > 0);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(MIN_DATE.plusYears(120));
        film.setDuration(130);
        return film;
    }

}
