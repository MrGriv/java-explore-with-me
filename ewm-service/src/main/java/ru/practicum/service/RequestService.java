package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ResponseEntity<ParticipationRequestDto> add(Long userId, Long eventId);

    List<ParticipationRequestDto> get(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);
}
