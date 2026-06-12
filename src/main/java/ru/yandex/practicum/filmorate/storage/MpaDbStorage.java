package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id",
                (rs, rowNum) -> {
                    Mpa m = new Mpa();
                    m.setId(rs.getInt("id"));
                    m.setName(rs.getString("name"));
                    return m;
                });
    }

    public Optional<Mpa> findById(int id) {
        List<Mpa> list = jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?",
                (rs, rowNum) -> {
                    Mpa m = new Mpa();
                    m.setId(rs.getInt("id"));
                    m.setName(rs.getString("name"));
                    return m;
                }, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}