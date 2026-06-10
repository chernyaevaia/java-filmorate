package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User getValidUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@ya.ru");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateValidUser() throws Exception {
        when(userService.create(any())).thenReturn(getValidUser());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getValidUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldFailWhenEmailInvalid() throws Exception {
        User user = getValidUser();
        user.setEmail("invalid-email");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenLoginHasSpaces() throws Exception {
        User user = getValidUser();
        user.setLogin("test login");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenBirthdayInFuture() throws Exception {
        User user = getValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenBirthdayIsNull() throws Exception {
        User user = getValidUser();
        user.setBirthday(null);
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() throws Exception {
        User user = getValidUser();
        user.setName("   ");
        User expected = getValidUser();
        expected.setName("testLogin");
        when(userService.create(any())).thenReturn(expected);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testLogin"));
    }
}