package ru.practicum.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatsStorageImpl implements StatsStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final String INSERT_STATISTICS_SQL = "INSERT INTO statistics (application, uri, ip, timestamp)" +
            " VALUES (:app, :uri, :ip, :timestamp)";
    private static final String SELECT_STATISTICS_ALL_SQL = "SELECT application, uri, COUNT(uri) " +
            "FROM statistics AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY uri, application " +
            "ORDER BY COUNT(uri) DESC ";
    private static final String SELECT_STATISTICS_ALL_UNIQUE_SQL = "SELECT DISTINCT application, uri, COUNT(DISTINCT ip) " +
            "FROM statistics AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY uri, application " +
            "ORDER BY COUNT(ip) DESC ";
    private static final String SELECT_STATISTICS_WITH_URIS_UNIQUE_IP_SQL = "SELECT application, uri, COUNT(DISTINCT ip) " +
            "FROM statistics AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN (:uris) " +
            "GROUP BY uri, application " +
            "ORDER BY COUNT(ip) DESC ";
    private static final String SELECT_STATISTICS_WITH_URIS_SQL = "SELECT application, uri, COUNT(uri) " +
            "FROM statistics AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN (:uris) " +
            "GROUP BY uri, application " +
            "ORDER BY COUNT(uri) DESC ";
    private static final String SELECT_STATISTICS_WITH_URI_IP_SQL = "SELECT application, uri, COUNT(uri) " +
            "FROM statistics AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN (:uris) AND s.ip = :ip " +
            "GROUP BY uri, application " +
            "ORDER BY COUNT(uri) DESC ";

    @Override
    public ResponseEntity<Void> hit(EndpointHit hit) {
        Map<String, Object> argMap = new HashMap<>();
        argMap.put("app", hit.getApp());
        argMap.put("uri", hit.getUri());
        argMap.put("ip", hit.getIp());
        argMap.put("timestamp", LocalDateTime.parse(hit.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jdbcTemplate.update(INSERT_STATISTICS_SQL, argMap);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<ViewStats>> stats(LocalDateTime start,
                                                 LocalDateTime end,
                                                 List<String> uris,
                                                 boolean unique,
                                                 String ip) {
        SqlRowSet rs;
        Map<String, Object> argMap = new HashMap<>();
        if ((uris.isEmpty()) && !unique) {
            argMap.put("start", start);
            argMap.put("end", end);
            rs = jdbcTemplate.queryForRowSet(SELECT_STATISTICS_ALL_SQL, argMap);
        } else if (uris.isEmpty()) {
            argMap.put("start", start);
            argMap.put("end", end);
            rs = jdbcTemplate.queryForRowSet(SELECT_STATISTICS_ALL_UNIQUE_SQL, argMap);
        } else if (unique) {
            argMap.put("start", start);
            argMap.put("end", end);
            argMap.put("uris", uris);
            rs = jdbcTemplate.queryForRowSet(SELECT_STATISTICS_WITH_URIS_UNIQUE_IP_SQL, argMap);
        } else {
            if (ip == null) {
                argMap.put("start", start);
                argMap.put("end", end);
                argMap.put("uris", uris);
                rs = jdbcTemplate.queryForRowSet(SELECT_STATISTICS_WITH_URIS_SQL, argMap);
            } else {
                argMap.put("start", start);
                argMap.put("end", end);
                argMap.put("uris", uris);
                argMap.put("ip", ip);
                rs = jdbcTemplate.queryForRowSet(SELECT_STATISTICS_WITH_URI_IP_SQL, argMap);
            }
        }
        List<ViewStats> stats = new ArrayList<>();
        while (rs.next()) {
            stats.add(ViewStats.builder()
                    .app(rs.getString(1))
                    .uri(rs.getString(2))
                    .hits(rs.getLong(3))
                    .build());
        }
        System.out.println(stats);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}
