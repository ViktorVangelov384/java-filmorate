package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreDbStorage genreStorage;

    @Autowired
    public GenreController(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return genreStorage.getAll();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {

        Genre genre = genreStorage.getById(id);
        if (genre == null) {
            throw new NotFoundException("Такого жанра нет");
        }
        return genreStorage.getById(id);
    }
}
