package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    ResponseEntity<Void> hit(EndpointHit hit);

    ResponseEntity<List<ViewStats>> stats(LocalDateTime start,
                                          LocalDateTime end,
                                          List<String> uris,
                                          boolean unique,
                                          String ip);
}
