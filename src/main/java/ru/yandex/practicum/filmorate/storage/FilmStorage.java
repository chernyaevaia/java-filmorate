package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Film delete(int id);

    Optional<Film> getById(int id);

    Collection<Film> getAll();

    List<Film> getPopular(int count);
}