package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreDbStorage genreStorage;

    @GetMapping
    public List<Genre> getAll() {
        return genreStorage.findAll();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable int id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр не найден"));
    }
}