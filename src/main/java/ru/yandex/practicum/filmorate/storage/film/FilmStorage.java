package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film getById(int id);

    List<Film> getAll();

    void delete(int id);

    Set<Integer> getLikes(int filmId);
}
