package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Constants;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MPAController {

    @GetMapping
    public List<Mpa> getAllMpa() {
        return Constants.DEFAULT_MPA_RATINGS;
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        return Constants.DEFAULT_MPA_RATINGS.stream()
                .filter(mpa -> mpa.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с id " + id + " не найден"));
    }
}
