package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.Request;

import java.util.List;

public interface RequestStorage extends JpaRepository<Request, Long>  {
    Request findByRequesterAndEvent(User requester, Event event);

    List<Request> findAllByRequester(User requester);

    @Query("select r from Request r " +
            "join r.event as e " +
            "where e.initiator.id = ?1 and e.id = ?2 ")
    List<Request> findAllRequestsOfEventByInitiator(Long requesterId, Long eventId);

    @Query("select r from Request r " +
            "join r.event as e " +
            "where r.id in ?1 and e.id = ?2 and e.initiator.id = ?3")
    List<Request> findAllByEventAndRequesterIn(List<Long> requestIds, Long eventId, Long initiatorId);
}
