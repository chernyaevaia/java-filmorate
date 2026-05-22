package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм с id={}", film.getId());
            throw new ValidationException("Фильм с id=" + film.getId() + " не найден");
        }
        validate(film);
        films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film.getName());
        return film;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка: пустое название");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка: описание > 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Ошибка: дата релиза раньше {}", CINEMA_BIRTHDAY);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Ошибка продолжительность <= 0");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}