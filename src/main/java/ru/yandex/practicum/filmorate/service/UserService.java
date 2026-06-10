package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().add((long) friendId);
        friend.getFriends().add((long) userId);
        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().remove((long) friendId);
        friend.getFriends().remove((long) userId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        return user.getFriends().stream()
                .map(id -> userStorage.getById(id.intValue()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Long> userFriends = userStorage.getById(userId).getFriends();
        Set<Long> otherFriends = userStorage.getById(otherId).getFriends();
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(id -> userStorage.getById(id.intValue()))
                .collect(Collectors.toList());
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public List<User> getAll() {
        return List.copyOf(userStorage.getAll());
    }
}