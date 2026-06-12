package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("User added: {}", user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("User updated: {}", user.getLogin());
        return user;
    }

    @Override
    public User delete(int id) {
        User user = users.remove(id);
        log.info("User deleted: {}", user.getLogin());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Set<Integer> friends = user.getFriends();
        if (friends == null) {
            friends = new HashSet<>();
            user.setFriends(friends);
        }
        friends.add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        Set<Integer> friends = user.getFriends();
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Set<Integer> friendIds = user.getFriends();
        if (friendIds == null || friendIds.isEmpty()) {
            return Collections.emptyList();
        }
        return friendIds.stream()
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        User other = getById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> otherFriends = other.getFriends();
        if (userFriends == null || otherFriends == null) {
            return Collections.emptyList();
        }

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}