package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.storage.StatsStorage;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsStorage statsStorage;

    @Override
    public ResponseEntity<Void> hit(EndpointHit hit) {
        log.info("Endpoint hit: {}", hit);
        return statsStorage.hit(hit);
    }

    @Override
    public  ResponseEntity<List<ViewStats>> stats(LocalDateTime start,
                                                  LocalDateTime end,
                                                  List<String> uris,
                                                  boolean unique,
                                                  String ip) {
        if (end.isBefore(start)) {
            throw new ValidationException("Дата конца не может быть раньше даты начала");
        }
        log.info("start: {}, end {}, uris: {}, unique: {}, ip: {}", start, end, uris, unique, ip);
        return statsStorage.stats(start, end, uris, unique, ip);
    }
}
