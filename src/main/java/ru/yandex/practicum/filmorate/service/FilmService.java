package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaDbStorage mpaStorage,
                       GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public void addLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Count must be positive");
        }
        return filmStorage.getPopular(count);
    }

    public Film create(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        getFilmOrThrow(film.getId());
        validateMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return getFilmOrThrow(id);
    }

    public List<Film> getAll() {
        return List.copyOf(filmStorage.getAll());
    }

    private Film getFilmOrThrow(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Film with id=" + id + " not found"));
    }

    private User getUserOrThrow(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException(" Рейтинг не найден"));
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId())
                    .orElseThrow(() -> new NotFoundException("Жанр не найден"));
            }
        }
    }
}