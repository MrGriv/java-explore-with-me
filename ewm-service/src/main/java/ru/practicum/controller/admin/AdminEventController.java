package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequestDto;
import ru.practicum.model.event.EventState;
import ru.practicum.service.EventService;
import ru.practicum.util.ApiPathConstants;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ADMIN_EVENTS_PATH)
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getWithFilters(@RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<EventState> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                 LocalDateTime rangeStart,
                                             @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                 LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return eventService.getWithFilters(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping(ApiPathConstants.EVENT_ID_PATH)
    public EventFullDto adminUpdate(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequestDto updateEventDto) {
        return eventService.adminUpdate(updateEventDto, eventId);
    }

}
