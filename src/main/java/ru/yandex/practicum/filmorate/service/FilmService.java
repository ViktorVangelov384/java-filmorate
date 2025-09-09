package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreDbStorage genreStorage,
                       MpaDbStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film create(Film film) {
        validateMpa(film.getMpa());
        validateGenre(film.getGenres());
        try {
            if (film.getMpa() != null) {
                Mpa fullMpa = mpaStorage.getById(film.getMpa().getId());
                film.setMpa(fullMpa);
            }

            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                Set<Genre> fullGenres = film.getGenres().stream()
                        .map(genre -> genreStorage.getById(genre.getId()))
                        .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
                film.setGenres(fullGenres);
            } else {
                film.setGenres(new TreeSet<>());
            }
            return filmStorage.create(film);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA рейтинг или жанр не найден");
        }
    }

    public Film update(Film film) {
        validateMpa(film.getMpa());
        validateGenre(film.getGenres());

        try {
            filmStorage.getById(film.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        if (film.getMpa() != null) {
            Mpa fullMpa = mpaStorage.getById(film.getMpa().getId());
            film.setMpa(fullMpa);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> fullGenres = film.getGenres().stream()
                    .map(genre -> genreStorage.getById(genre.getId()))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
            film.setGenres(fullGenres);
        } else {
            film.setGenres(new TreeSet<>());
        }
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        try {
            return filmStorage.getById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    public void addLike(int filmId, int userId) {
        try {
            filmStorage.getById(filmId);
            userStorage.getById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Ресурс не найден");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        try {
            filmStorage.getById(filmId);
            userStorage.getById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Ресурс не найден");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    public void addGenreToFilm(int filmId, int genreId) {
        Film film = filmStorage.getById(filmId);
        Genre genre = genreStorage.getById(genreId);
        film.getGenres().add(genre);
        filmStorage.update(film);
    }

    public void removeGenreFromFilm(int filmId, int genreId) {
        Film film = filmStorage.getById(filmId);
        film.getGenres().removeIf(g -> g.getId() == genreId);
        filmStorage.update(film);
    }

    private void validateMpa(Mpa mpa) {
        if (mpa != null) {
            try {
                mpaStorage.getById(mpa.getId());
            } catch (EmptyResultDataAccessException e) {
                throw new NotFoundException("Рейтинг MPA с id " + mpa.getId() + " не найден");
            }
        }
    }

    private void validateGenre(Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                try {
                    genreStorage.getById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
    }
}
