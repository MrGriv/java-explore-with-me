package ru.practicum.storage;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsStorage  {
    void hit(EndpointHit hit);

    List<ViewStats> stats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}