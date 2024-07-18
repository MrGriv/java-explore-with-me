package ru.practicum.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.model.user.ShowEventsState;

import java.util.List;

public interface FriendService {
    void add(Long userId, Long friendId);
    
    List<EventFullDto> getFriendParticipations(Long userId, Long friendId, int from, int size);

    void changeEventsVisibility(Long userId, ShowEventsState state, List<Long> events);
}
