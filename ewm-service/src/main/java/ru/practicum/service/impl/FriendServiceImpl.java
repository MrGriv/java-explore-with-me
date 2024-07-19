package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.Request;
import ru.practicum.model.user.ShowEventsState;
import ru.practicum.model.user.User;
import ru.practicum.service.FriendService;
import ru.practicum.storage.CategoryStorage;
import ru.practicum.storage.EventStorage;
import ru.practicum.storage.RequestStorage;
import ru.practicum.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.service.impl.EventServiceImpl.setCategoriesAndReturnList;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final UserStorage userStorage;
    private final EventStorage eventStorage;
    private final CategoryStorage categoryStorage;
    private final CategoryMapper categoryMapper;
    private final EventMapper eventMapper;
    private final RequestStorage requestStorage;

    @Override
    public void add(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + friendId + " не найден"));
        List<Long> userFriends = user.getFriends().isEmpty() ? new ArrayList<>() : user.getFriends();
        userFriends.add(friend.getId());
        user.setFriends(userFriends);
        userStorage.save(user);
        List<Long> friendFriends = friend.getFriends().isEmpty() ? new ArrayList<>() : friend.getFriends();
        friendFriends.add(user.getId());
        friend.setFriends(friendFriends);
        userStorage.save(friend);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getFriendParticipations(Long userId, Long friendId, int from, int size) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + friendId + " не найден"));
        if (!friend.getFriends().contains(user.getId())) {
            throw new ConflictException("Friends: Пользователь с id=" + userId
                    + "нет в друзьях у пользователя c id=" + friend);
        }
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (friend.getShowEventsState().equals(ShowEventsState.ALL) ||
                friend.getShowEventsState().equals(ShowEventsState.CHOSEN)) {
            Page<Event> events = eventStorage.findAllByIdIn(friend.getUserEvents(), page);
            return events.isEmpty() ? new ArrayList<>() : setCategoriesAndReturnList(events.toList(),
                    categoryStorage,
                    categoryMapper,
                    eventMapper);
        } else {
            throw new ConflictException("Friend: пользователь скрыл посещаемые события");
        }
    }

    @Override
    public void changeEventsVisibility(Long userId, ShowEventsState state, List<Long> events) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        if (state.equals(ShowEventsState.ALL)) {
            List<Request> requests = requestStorage.findAllByRequesterAnsStatusConfirmed(user.getId());
            if (!requests.isEmpty()) {
                List<Long> userEvents = new ArrayList<>();
                for (Request request : requests) {
                    userEvents.add(request.getEvent().getId());
                }
                user.setShowEventsState(ShowEventsState.ALL);
                user.setUserEvents(userEvents);
                userStorage.save(user);
            }
        } else if (state.equals(ShowEventsState.CHOSEN)) {
            if (events == null || events.isEmpty()) {
                throw new ConflictException("User: При смене статуса на CHOSEN нужно выбрать события" +
                        " для отображения друзьям");
            }
            List<Request> requests = requestStorage.findAllByEventId(events, user.getId());

            if (requests.isEmpty()) {
                throw new ConflictException("User: в списке событий не обнаружены указанные события: " + events);
            }
            if (requests.size() != events.size()) {
                throw new ConflictException("User: Не все события из списка будут или были посещены пользователем");
            }
            user.setUserEvents(events);
            user.setShowEventsState(ShowEventsState.CHOSEN);
            userStorage.save(user);
        } else {
            user.setShowEventsState(ShowEventsState.HIDE);
            user.setUserEvents(new ArrayList<>());
            userStorage.save(user);
        }
    }
}