package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film getValidFilm() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    @Test
    void shouldCreateValidFilm() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getValidFilm())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldFailWhenNameIsBlank() throws Exception {
        Film film = getValidFilm();
        film.setName("   ");
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenDescriptionTooLong() throws Exception {
        Film film = getValidFilm();
        film.setDescription("a".repeat(201));
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassWhenDescriptionExactly200() throws Exception {
        Film film = getValidFilm();
        film.setDescription("a".repeat(200));
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenReleaseDateBefore1895() throws Exception {
        Film film = getValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassWhenReleaseDateIsCinemaBirthday() throws Exception {
        Film film = getValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenDurationIsZero() throws Exception {
        Film film = getValidFilm();
        film.setDuration(0);
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }
}