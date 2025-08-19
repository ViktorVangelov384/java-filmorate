package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getInt("mpa_id"), rs.getString("name"), rs.getString("description"))
        );
    }

    public Mpa getById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new Mpa(rs.getInt("mpa_id"), rs.getString("name"), rs.getString("description")),
                id
        );
    }
}

