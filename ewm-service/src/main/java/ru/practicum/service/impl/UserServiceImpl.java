package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.UserDto;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.service.UserService;
import ru.practicum.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public ResponseEntity<UserDto> add(UserDto userDto) {
        User user = userStorage.save(userMapper.toEntity(userDto));
        return new ResponseEntity<>(userMapper.toDto(user), HttpStatus.CREATED);
    }

    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (ids.isEmpty()) {
            return userStorage.findAll(page).map(userMapper::toDto).getContent();
        } else {
            Page<User> users = userStorage.findAllByIdIn(ids, page);
            return users.isEmpty() ? new ArrayList<>() : users.map(userMapper::toDto).getContent();
        }
    }

    @Override
    public ResponseEntity<Void> delete(Long userId) {
        userStorage.deleteById(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
