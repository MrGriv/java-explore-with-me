package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    ResponseEntity<UserDto> add(UserDto userDto);

    ResponseEntity<List<UserDto>> get(List<Long> ids, int from, int size);

    ResponseEntity<Void> delete(Long userId);
}
