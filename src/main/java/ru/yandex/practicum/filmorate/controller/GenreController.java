package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Constants;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    @GetMapping
    public List<Genre> getAllGenres() {
        return Constants.DEFAULT_GENRES;
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        return Constants.DEFAULT_GENRES.stream()
                .filter(genre -> genre.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}
