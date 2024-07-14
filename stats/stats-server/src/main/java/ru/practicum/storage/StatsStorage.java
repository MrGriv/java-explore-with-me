package ru.practicum.storage;

import org.springframework.http.ResponseEntity;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsStorage  {
    ResponseEntity<Void> hit(EndpointHit hit);

    ResponseEntity<List<ViewStats>> stats(LocalDateTime start,
                                          LocalDateTime end,
                                          List<String> uris,
                                          boolean unique,
                                          String ip);
}
