package ru.practicum.controller.pvt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequestDto;
import ru.practicum.service.EventService;
import ru.practicum.util.ApiPathConstants;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.PRIVATE_USERS_EVENTS)
public class PrivateUserEventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventFullDto> add(@PathVariable Long id,
                                            @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.add(newEventDto, id);
    }

    @GetMapping
    public List<EventFullDto> get(@PathVariable Long id,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        return eventService.get(id, from, size);
    }

    @GetMapping(ApiPathConstants.EVENT_ID_PATH)
    public EventFullDto getById(@PathVariable Long id, @PathVariable Long eventId) {
        return eventService.getById(id, eventId);
    }

    @PatchMapping(ApiPathConstants.EVENT_ID_PATH)
    public EventFullDto update(@Valid @RequestBody UpdateEventUserRequestDto updateEventUserRequestDto,
                               @PathVariable Long id,
                               @PathVariable Long eventId) {
        return eventService.userUpdate(updateEventUserRequestDto, id, eventId);
    }

    @GetMapping(ApiPathConstants.EVENT_ID_REQUESTS_PATH)
    public List<ParticipationRequestDto> getInitiatorRequests(@PathVariable Long id, @PathVariable Long eventId) {
        return eventService.getInitiatorRequests(id, eventId);
    }

    @PatchMapping(ApiPathConstants.EVENT_ID_REQUESTS_PATH)
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long id,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest requests) {
        return eventService.changeRequestStatus(id, eventId, requests);
    }
}
