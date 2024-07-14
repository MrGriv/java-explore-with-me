package ru.practicum.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;

import java.util.List;
import java.util.Optional;

public interface EventStorage extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Page<Event> findAllByInitiator(User user, Pageable pageable);

    Event findByInitiatorAndId(User initiator, Long eventId);

    @Query("select e from Event e " +
            "where e.id = ?1 and e.state = 'PUBLISHED'")
    Optional<Event> findPublishedEventById(Long eventId);

    List<Event> findAllByIdIn(List<Long> id);
}
