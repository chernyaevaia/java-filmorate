package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaDbStorage mpaStorage;

    @GetMapping
    public List<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable int id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new ru.yandex.practicum.filmorate.exception.NotFoundException("Рейтинг не найден"));
    }
}