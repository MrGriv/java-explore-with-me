package ru.practicum.controller.pvt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;
import ru.practicum.util.ApiPathConstants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.USER_ID_REQUEST_PATH)
public class PrivateUserRequestController {
    private final RequestService requestService;

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> add(@PathVariable Long id,
                                                       @RequestParam Long eventId) {
        return requestService.add(id, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> get(@PathVariable Long id) {
        return requestService.get(id);
    }

    @PatchMapping(ApiPathConstants.ID_CANCEL_PATH)
    public ParticipationRequestDto cancel(@PathVariable Long id, @PathVariable Long requestId) {
        return requestService.cancel(id, requestId);
    }
}
