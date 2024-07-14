package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.service.RequestService;
import ru.practicum.storage.EventStorage;
import ru.practicum.storage.RequestStorage;
import ru.practicum.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestStorage requestStorage;
    private final UserStorage userStorage;
    private final EventStorage eventStorage;
    private final RequestMapper requestMapper;

    @Override
    public ResponseEntity<ParticipationRequestDto> add(Long userId, Long eventId) {
        User requester = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        Event event = eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Request: Нельзя участвовать в неопубликованном событии eventId=" + eventId);
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new ConflictException("Request: Достигнут лимит запросов на участие ParticipantLimit="
                    + event.getParticipantLimit());
        }
        Request sameRequest = requestStorage.findByRequesterAndEvent(requester, event);
        if (sameRequest != null) {
            throw new ConflictException("Request: Пользователь с id=" + userId +
                    " уже подал заявку на событие eventId=" + eventId);
        }
        Event initiatorOfEvent = eventStorage.findByInitiatorAndId(requester, eventId);
        if (initiatorOfEvent != null) {
            throw new ConflictException("Request: Инициатор события id=" + userId +
                    " не может добавить запрос на участие в своём событии eventId=" + eventId);
        }
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventStorage.save(event);
        }
        return new ResponseEntity<>(requestMapper.toDto(requestStorage.save(request)), HttpStatus.CREATED);
    }

    @Override
    public List<ParticipationRequestDto> get(Long userId) {
        User requester = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        List<Request> requests = requestStorage.findAllByRequester(requester);
        return requests.isEmpty() ? new ArrayList<>() : requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        Request request = requestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request: Запрос на участие с id=" + requestId + " не найден"));
        request.setStatus(RequestStatus.CANCELED);
        Event event = eventStorage.getById(requestId);
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        return requestMapper.toDto(requestStorage.save(request));
    }
}
