package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        if (rs.getString("mpa_name") != null) {
            mpa.setName(rs.getString("mpa_name"));
            mpa.setDescription(rs.getString("mpa_description"));
        }
        film.setMpa(mpa);

        return film;
    };

    @Override
    public Film create(Film film) {
        if (film.getMpa() == null) {
            throw new NotFoundException("Mpa обязательное");
        }
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        updateGenres(film);
        updateLikes(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";

        jdbcTemplate.update(sql, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateGenres(film);
        updateLikes(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.*, m.mpa_id, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";
        Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);

        if (film != null) {
            loadGenres(film);
            loadLikes(film);
        }

        return film;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, m.mpa_id, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        films.forEach(film -> {
            loadGenres(film);
            loadLikes(film);
        });

        return films;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Set<Integer> getLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getInt("user_id"), filmId));
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";
        Set<Genre> genres = new HashSet<>();

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            genres.add(genre);
            return null;
        }, film.getId());

        film.setGenres(genres);
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Integer> likes = new HashSet<>();

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            likes.add(rs.getInt("user_id"));
            return null;
        }, film.getId());

        film.setLikes(likes);
    }

    private void updateGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(insertSql, film.getId(), genre.getId());
            });
        }
    }

    private void updateLikes(Film film) {
        String deleteSql = "DELETE FROM likes WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            String insertSql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            film.getLikes().forEach(userId -> {
                jdbcTemplate.update(insertSql, film.getId(), userId);
            });

        }
    }
}

