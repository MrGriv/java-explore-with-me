package ru.practicum.controller.pvt;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.model.user.ShowEventsState;
import ru.practicum.service.FriendService;
import ru.practicum.util.ApiPathConstants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.USER_ID_FRIEND_PATH)
public class PrivateFriendController {
    private final FriendService friendService;

    @PostMapping(ApiPathConstants.FRIEND_ID)
    public void add(@PathVariable Long id, @PathVariable Long friendId) {
        friendService.add(id, friendId);
    }

    @GetMapping(ApiPathConstants.FRIEND_ID)
    public List<EventFullDto> getFriendParticipations(@PathVariable Long id,
                                                      @PathVariable Long friendId,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "10") int size) {
        return friendService.getFriendParticipation(id, friendId, from, size);
    }

    @PatchMapping
    public void changeEventsVisibility(@PathVariable Long id,
                                       @RequestParam ShowEventsState state,
                                       @RequestParam(required = false) List<Long> events) {
        friendService.changeEventsVisibility(id, state, events);
    }
}
