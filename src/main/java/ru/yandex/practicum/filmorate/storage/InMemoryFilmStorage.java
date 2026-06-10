package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Фильм обновлён: {}", film.getName());
        return film;
    }

    @Override
    public Film delete(int id) {
        Film film = films.remove(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        log.info("Фильм удалён: {}", film.getName());
        return film;
    }

    @Override
    public Film getById(int id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }
}