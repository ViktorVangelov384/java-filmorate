package ru.yandex.practicum.filmorate.model;

import java.util.*;

public class Constants {
    public static final List<Genre> DEFAULT_GENRES = Arrays.asList(
            new Genre(1, "Комедия"),
            new Genre(2, "Драма"),
            new Genre(3, "Мультфильм"),
            new Genre(4, "Триллер"),
            new Genre(5, "Документальный"),
            new Genre(6, "Боевик")
    );

    public static final List<Mpa> DEFAULT_MPA_RATINGS = Arrays.asList(
            new Mpa(1, "G", "У фильма нет возрастных ограничений"),
            new Mpa(2, "PG", "Детям рекомендуется смотреть фильм с родителями"),
            new Mpa(3, "PG-13", "Детям до 13 лет просмотр не желателен"),
            new Mpa(4, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
            new Mpa(5, "NC-17", "Лицам до 18 лет просмотр запрещён")
    );
}
