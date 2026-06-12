package ru.yandex.practicum.filmorate.storage;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id",
                (rs, rowNum) -> {
                    Genre g = new Genre();
                    g.setId(rs.getInt("id"));
                    g.setName(rs.getString("name"));
                    return g;
                });
    }

    public Optional<Genre> findById(int id) {
        List<Genre> list = jdbcTemplate.query("SELECT * FROM genres WHERE id = ?",
                (rs, rowNum) -> {
                    Genre g = new Genre();
                    g.setId(rs.getInt("id"));
                    g.setName(rs.getString("name"));
                    return g;
                }, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}