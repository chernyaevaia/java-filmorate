package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert filmInsert;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    @Transactional
    public Film create(Film film) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpa_id", film.getMpa() != null ? film.getMpa().getId() : null);

        int id = filmInsert.executeAndReturnKey(params).intValue();
        film.setId(id);

        insertFilmGenres(id, film.getGenres());
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        int rows = jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?",
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        if (rows == 0) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        insertFilmGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    @Transactional
    public Film delete(int id) {
        Film film = getById(id)
                .orElseThrow(() -> new NotFoundException("Film with id=" + id + " not found"));
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.id as mpa_id_ref, m.name as mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        film.setGenres(getGenresForFilms(List.of(id)).getOrDefault(id, new LinkedHashSet<>()));
        film.setLikes(getLikesForFilms(List.of(id)).getOrDefault(id, new HashSet<>()));
        return Optional.of(film);
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.id as mpa_id_ref, m.name as mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        populateGenresAndLikes(films);
        return films;
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.id as mpa_id_ref, m.name as mpa_name, COUNT(fl.user_id) as like_count " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.id, m.name " +
                "ORDER BY like_count DESC, f.id ASC LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        populateGenresAndLikes(films);
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update(
                "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId, userId);
    }

    private void insertFilmGenres(int filmId, Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        Set<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Integer> uniqueGenreIds = new ArrayList<>(genreIds);
        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, uniqueGenreIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return uniqueGenreIds.size();
                    }
                });
    }

    private void populateGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Integer> filmIds = films.stream().map(Film::getId).toList();
        Map<Integer, Set<Genre>> genresMap = getGenresForFilms(filmIds);
        Map<Integer, Set<Integer>> likesMap = getLikesForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private Map<Integer, Set<Genre>> getGenresForFilms(List<Integer> filmIds) {
        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id IN (" + inClause + ") " +
                "ORDER BY g.id";

        Map<Integer, Set<Genre>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Genre g = new Genre();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(g);
        }, filmIds.toArray());
        return result;
    }

    private Map<Integer, Set<Integer>> getLikesForFilms(List<Integer> filmIds) {
        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inClause + ")";

        Map<Integer, Set<Integer>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());
        return result;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", java.time.LocalDate.class));
        film.setDuration(rs.getInt("duration"));

        if (rs.getObject("mpa_id") != null) {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id_ref"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
        return film;
    }
}