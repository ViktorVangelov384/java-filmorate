package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User {
    private int id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @")
    private String email;

    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @JsonIgnore
    private Map<Integer, FriendshipStatus> friends = new HashMap<>();

    @JsonIgnore
    public Set<Integer> getConfirmedFriendIds() {
        return friends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Integer> getPendingFriendIds() {
        return friends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.PENDING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
