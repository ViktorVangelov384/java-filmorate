package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name"))
        );
    }

    public Genre getById(int id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new Genre(rs.getInt("genre_id"), rs.getString("name")),
                id
        );
    }
}

