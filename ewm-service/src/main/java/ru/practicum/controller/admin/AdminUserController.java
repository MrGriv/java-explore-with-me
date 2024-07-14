package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;
import ru.practicum.util.ApiPathConstants;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ADMIN_USERS_PATH)
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> add(@Valid @RequestBody UserDto userDto) {
        return userService.add(userDto);
    }

    @GetMapping
    public List<UserDto> get(@RequestParam(defaultValue = "") List<Long> ids,
                             @RequestParam(defaultValue = "0") int from,
                             @RequestParam(defaultValue = "10") int size) {
        return userService.get(ids, from, size);
    }

    @DeleteMapping(ApiPathConstants.BY_ID_PATH)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return userService.delete(id);
    }
}
