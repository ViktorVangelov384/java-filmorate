package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MPAController {

    private final MpaDbStorage mpaStorage;

    @Autowired
    public MPAController(MpaDbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        return mpaStorage.getAll();
    }

    /*@GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        return mpaStorage.getById(id);
    }*/

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        Mpa mpa = mpaStorage.getById(id);
        if (mpa == null) {
            throw new NotFoundException("MPA рейтинг с id " + id + " не найден");
        }
        return mpa;
    }
}
