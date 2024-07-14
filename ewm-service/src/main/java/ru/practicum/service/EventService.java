package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequestDto;
import ru.practicum.dto.event.UpdateEventUserRequestDto;
import ru.practicum.model.event.EventState;
import ru.practicum.model.event.SortState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    ResponseEntity<EventFullDto> add(NewEventDto newEventDto, Long userId);

    List<EventFullDto> get(Long userId, int from, int size);

    EventFullDto getById(Long userId, Long eventId);

    EventFullDto userUpdate(UpdateEventUserRequestDto updateEventUserRequestDto, Long userId, Long eventId);

    List<EventFullDto> getWithFilters(List<Long> users,
                                      List<EventState> states,
                                      List<Long> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      int from,
                                      int size);

    EventFullDto adminUpdate(UpdateEventAdminRequestDto updateEventDto, Long eventId);

    List<ParticipationRequestDto> getInitiatorRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                       Long eventId,
                                                       EventRequestStatusUpdateRequest requests);

    List<EventFullDto> userGetByFilters(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Boolean onlyAvailable,
                                        SortState sort,
                                        int from,
                                        int size,
                                        HttpServletRequest request);

    EventFullDto userGetById(Long eventId, HttpServletRequest request);
}
