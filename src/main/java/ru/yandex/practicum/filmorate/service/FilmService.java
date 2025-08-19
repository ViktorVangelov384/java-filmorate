package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Constants;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateMpa(film.getMpa());
        validateGenre(film.getGenres());

        if (film.getMpa() != null) {
            Mpa fullMpa = Constants.DEFAULT_MPA_RATINGS.stream()
                    .filter(m -> m.getId() == film.getMpa().getId())
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("MPA рейтинг не найден"));
            film.setMpa(fullMpa);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> fullGenres = film.getGenres().stream()
                    .map(genre -> Constants.DEFAULT_GENRES.stream()
                            .filter(g -> g.getId() == genre.getId())
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Жанр не найден")))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
            film.setGenres(fullGenres);
        } else {
            film.setGenres(new HashSet<>());
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateMpa(film.getMpa());
        validateGenre(film.getGenres());
        if (filmStorage.getById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        if (film.getMpa() != null) {
            Mpa fullMpa = Constants.DEFAULT_MPA_RATINGS.stream()
                    .filter(m -> m.getId() == film.getMpa().getId())
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("MPA рейтинг не найден"));
            film.setMpa(fullMpa);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> fullGenres = film.getGenres().stream()
                    .map(genre -> Constants.DEFAULT_GENRES.stream()
                            .filter(g -> g.getId() == genre.getId())
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Жанр не найден")))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
            film.setGenres(fullGenres);
        } else {
            film.setGenres(new HashSet<>());
        }
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }
        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size())) // Сортируем по убыванию
                .limit(count > 0 ? count : 10) // По умолчанию 10, если count не указан
                .collect(Collectors.toList());
    }

    public void addGenreToFilm(int filmId, int genreId) {
        Film film = filmStorage.getById(filmId);
        Genre genre = findGenreById(genreId);
        film.getGenres().add(genre);
        filmStorage.update(film);
    }

    public void removeGenreFromFilm(int filmId, int genreId) {
        Film film = filmStorage.getById(filmId);
        film.getGenres().removeIf(g -> g.getId() == genreId);
        filmStorage.update(film);
    }

    private Genre findGenreById(int genreId) {
        return Constants.DEFAULT_GENRES.stream()
                .filter(g -> g.getId() == genreId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
    }

    private void validateMpa(Mpa mpa) {
        if (mpa != null) {
            boolean mpaExists = Constants.DEFAULT_MPA_RATINGS.stream()
                    .anyMatch(m -> m.getId() == mpa.getId());
            if (!mpaExists) {
                throw new NotFoundException("Рейтинг MPA с id " + mpa.getId() + " не найден");
            }
        }
    }

    private void validateGenre(Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                boolean genreExists = Constants.DEFAULT_GENRES.stream()
                        .anyMatch(g -> g.getId() == genre.getId());
                if (!genreExists) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
    }
}
