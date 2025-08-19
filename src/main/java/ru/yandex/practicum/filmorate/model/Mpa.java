package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Mpa {
    private int id;
    private String name;
    private String description;

    public Mpa() {
    }

    public Mpa(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
