package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь добавлен: {}", user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Пользователь обновлён: {}", user.getLogin());
        return user;
    }

    @Override
    public User delete(int id) {
        User user = users.remove(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        log.info("Пользователь удалён: {}", user.getLogin());
        return user;
    }

    @Override
    public User getById(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }
}