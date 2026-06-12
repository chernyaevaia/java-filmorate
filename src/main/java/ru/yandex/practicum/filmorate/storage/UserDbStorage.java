package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert userInsert;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    @Transactional
    public User create(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        params.put("login", user.getLogin());
        params.put("name", user.getName());
        params.put("birthday", user.getBirthday());

        int id = userInsert.executeAndReturnKey(params).intValue();
        user.setId(id);
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        int rows = jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        if (rows == 0) {
            throw new NotFoundException("User with id=" + user.getId() + " not found");
        }
        return user;
    }

    @Override
    @Transactional
    public User delete(int id) {
        User user = getById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.get(0);
        user.setFriends(getFriendsForUser(id));
        return Optional.of(user);
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        Map<Integer, Set<Integer>> friendsMap = getFriendsForUsers(
                users.stream().map(User::getId).toList());
        for (User user : users) {
            user.setFriends(friendsMap.getOrDefault(user.getId(), new HashSet<>()));
        }
        return users;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)",
                    userId, friendId);
        } catch (DuplicateKeyException e) {
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private Map<Integer, Set<Integer>> getFriendsForUsers(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = String.join(",", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT user_id, friend_id FROM friendships WHERE user_id IN (" + inClause + ")";

        Map<Integer, Set<Integer>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int userId = rs.getInt("user_id");
            int friendId = rs.getInt("friend_id");
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        }, userIds.toArray());
        return result;
    }

    private Set<Integer> getFriendsForUser(int userId) {
        return new HashSet<>(jdbcTemplate.query(
                "SELECT friend_id FROM friendships WHERE user_id = ?",
                (rs, rowNum) -> rs.getInt("friend_id"), userId));
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getObject("birthday", java.time.LocalDate.class));
        return user;
    }
}