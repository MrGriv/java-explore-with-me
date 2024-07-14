package ru.practicum.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.ViewStats;
import ru.practicum.dto.Location;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Category;
import ru.practicum.model.LocationDb;
import ru.practicum.model.User;
import ru.practicum.model.event.*;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.service.EventService;
import ru.practicum.storage.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final EventMapper eventMapper;
    private final LocationStorage locationStorage;
    private final LocationMapper locationMapper;
    private final CategoryStorage categoryStorage;
    private final CategoryMapper categoryMapper;
    private final RequestMapper requestMapper;
    private final RequestStorage requestStorage;
    private final StatsClient statsClient;
    private static final QEvent qEvent = QEvent.event;
    //Private

    @Override
    public ResponseEntity<EventFullDto> add(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("дата и время на которые намечено событие не может быть раньше," +
                    " чем через два часа от текущего момента. NewEventDate: " + newEventDto.getEventDate());
        }
        User initiator = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        Category category = categoryStorage.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category: Категория с id=" + newEventDto.getCategory()
                        + " не найдена"));
        LocationDb locationWithId = locationStorage.save(locationMapper.toEntity(newEventDto.getLocation()));
        Event event = eventMapper.toEntity(newEventDto);
        event.setLocation(locationWithId);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setConfirmedRequests(0);
        event.setViews(0);
        Event savedEvent = eventStorage.save(event);
        EventFullDto eventFullDto = eventMapper.toDto(savedEvent);
        eventFullDto.setCategory(categoryMapper.toDto(category));
        return new ResponseEntity<>(eventFullDto, HttpStatus.CREATED);
    }

    @Override
    public List<EventFullDto> get(Long userId, int from, int size) {
        User initiator = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Event> events = eventStorage.findAllByInitiator(initiator, page);
        return events.isEmpty() ? new ArrayList<>() : events.map(eventMapper::toDto).getContent();
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        Event event = eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        return eventMapper.toDto(event);
    }

    @Override
    public EventFullDto userUpdate(UpdateEventUserRequestDto updateEventDto, Long userId, Long eventId) {
        if (updateEventDto.getEventDate() != null) {
            if (updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("дата и время на которые намечено событие не может быть раньше, чем через" +
                        " два часа от текущего момента. NewEventDate: " + updateEventDto.getEventDate());
            }
        }
        Event event = eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event: Нельзя обновить событие в состоянии PUBLISHED");
        }
        setEventFields(event,
                updateEventDto.getAnnotation(),
                updateEventDto.getCategory(),
                updateEventDto.getDescription(),
                updateEventDto.getEventDate(),
                updateEventDto.getPaid(),
                updateEventDto.getParticipantLimit(),
                updateEventDto.getRequestModeration(),
                updateEventDto.getTitle());
        if (updateEventDto.getStateAction() != null) {
            if (updateEventDto.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        return getEventFullDto(event, updateEventDto.getCategory(), updateEventDto.getLocation());
    }

    @Override
    public List<ParticipationRequestDto> getInitiatorRequests(Long userId, Long eventId) {
        User initiator = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        Event event = eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        List<Request> requests = requestStorage.findAllRequestsOfEventByInitiator(initiator.getId(), event.getId());
        return requests.isEmpty() ? new ArrayList<>() : requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                                  Long eventId,
                                                              EventRequestStatusUpdateRequest incomingRequests) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User: Пользователь с id=" + userId + " не найден"));
        Event event = eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        List<Request> requests = requestStorage.findAllByEventAndRequesterIn(incomingRequests.getRequestIds(), eventId, userId);
        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new ConflictException("Request: Нельзя добавить участников на событие, если лимит участников" +
                    " исчерпан limit=" + event.getParticipantLimit());
        }
        int amountConfirmedRequests = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            for (Request request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Request: Нельзя изменить статус заявки id=" + request.getId() +
                            "который находится не в статусе PENDING");
                }
                amountConfirmedRequests = setRequestsStatus(incomingRequests,
                        amountConfirmedRequests,
                        confirmedRequests,
                        rejectedRequests,
                        request);
            }
        }
        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request: Нельзя изменить статус заявки id=" + request.getId() +
                        "который находится не в статусе PENDING");
            }
            if (amountConfirmedRequests < limit) {
                amountConfirmedRequests = setRequestsStatus(incomingRequests,
                        amountConfirmedRequests,
                        confirmedRequests,
                        rejectedRequests,
                        request);
            } else {
                rejectedRequests.add(request);
            }
        }
        event.setConfirmedRequests(amountConfirmedRequests);
        eventStorage.save(event);
        requestStorage.saveAll(rejectedRequests);
        requestStorage.saveAll(confirmedRequests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests.stream().map(requestMapper::toDto).collect(Collectors.toList()));
        result.setRejectedRequests(rejectedRequests.stream().map(requestMapper::toDto).collect(Collectors.toList()));
        return result;
    }

    private int setRequestsStatus(EventRequestStatusUpdateRequest incomingRequests,
                                  int amountConfirmedRequests,
                                  List<Request> confirmedRequests,
                                  List<Request> rejectedRequests,
                                  Request request) {
        if (incomingRequests.getStatus().equals(RequestStatus.CONFIRMED)) {
            request.setStatus(RequestStatus.CONFIRMED);
            confirmedRequests.add(request);
            ++amountConfirmedRequests;
        } else {
            request.setStatus(RequestStatus.REJECTED);
            rejectedRequests.add(request);
        }
        return amountConfirmedRequests;
    }

    //Admin
    @Override
    public List<EventFullDto> getWithFilters(List<Long> users,
                                             List<EventState> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             int from,
                                             int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        GetAdminEvent getAdminEvent = new GetAdminEvent(users, states, categories, rangeStart, rangeEnd);
        BooleanExpression conditions = makeAdminEventQueryFilters(getAdminEvent);
        Page<Event> events = eventStorage.findAll(conditions, page);
        return events.isEmpty() ? new ArrayList<>() : setCategoriesAndReturnList(events.toList());
    }

    private List<EventFullDto> setCategoriesAndReturnList(List<Event> events) {
        List<Long> categoriesId = new ArrayList<>();
        for (Event event : events) {
            categoriesId.add(event.getCategory());
        }
        Map<Long, CategoryDto> categories = categoryStorage.findAllById(categoriesId).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));
        return events.stream().map(eventMapper::toDto)
                .peek(eventFullDto -> eventFullDto.setCategory(categories.get(eventFullDto.getCategory().getId())))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto adminUpdate(UpdateEventAdminRequestDto updateEventDto, Long eventId) {
        if (updateEventDto.getEventDate() != null) {
            if (updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Event: Дата и время на которые намечено событие не может быть раньше," +
                        " чем через два часа от текущего момента. NewEventDate: " + updateEventDto.getEventDate());
            }
        }
        Event event = eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        setEventFields(event,
                updateEventDto.getAnnotation(),
                updateEventDto.getCategory(),
                updateEventDto.getDescription(),
                updateEventDto.getEventDate(),
                updateEventDto.getPaid(),
                updateEventDto.getParticipantLimit(),
                updateEventDto.getRequestModeration(),
                updateEventDto.getTitle());
        if (updateEventDto.getStateAction() != null) {
            if (updateEventDto.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConflictException("Event: Нельзя отменить опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            } else {
                if (event.getState().equals(EventState.PENDING)) {
                    event.setPublishedOn(LocalDateTime.now());
                    event.setState(EventState.PUBLISHED);
                } else {
                    throw new ConflictException("Event: Можно публиковать событие только в состоянии PENDING");
                }
            }
        }
        return getEventFullDto(event, updateEventDto.getCategory(), updateEventDto.getLocation());
    }

    private EventFullDto getEventFullDto(Event event, Long newCategory, Location location) {
        Category category = categoryStorage.findById(event.getCategory())
                .orElseThrow(() -> new NotFoundException("Category: Категория с id=" + newCategory
                        + " не найдена"));
        event.setLocation(location == null ? event.getLocation()
                : locationStorage.save(locationMapper.toEntity(location)));
        Event savedEvent = eventStorage.save(event);
        EventFullDto eventFullDto = eventMapper.toDto(savedEvent);
        eventFullDto.setCategory(categoryMapper.toDto(category));
        return eventFullDto;
    }

    private void setEventFields(Event event,
                                String annotation,
                                Long newCategory,
                                String description,
                                LocalDateTime eventDate,
                                Boolean paid,
                                Integer participantLimit,
                                Boolean requestModeration,
                                String title) {
        event.setAnnotation(annotation == null
                ? event.getAnnotation() : annotation);
        event.setCategory(newCategory == null ? event.getCategory() : newCategory);
        event.setDescription(description == null
                ? event.getDescription() : description);
        event.setEventDate(eventDate == null ? event.getEventDate() : eventDate);
        event.setPaid(paid == null ? event.getPaid() : paid);
        event.setParticipantLimit(participantLimit == null ? event.getParticipantLimit()
                : participantLimit);
        event.setRequestModeration(requestModeration == null ? event.getRequestModeration()
                : requestModeration);
        event.setTitle(title == null ? event.getTitle() : title);
    }

    //Public
    @Override
    public List<EventFullDto> userGetByFilters(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               SortState sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {
        if (rangeEnd != null && rangeStart != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException("Дата конца не может быть раньше даты начала");
            }
        }
        GetUserEvent getUserEvent = new GetUserEvent(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);
        BooleanExpression conditions = makeUserEventQueryFilters(getUserEvent);
        PageRequest page;
        if (sort.equals(SortState.EVENT_DATE)) {
            page = PageRequest.of(from > 0 ? from / size : 0, size,
                    Sort.by(Sort.Direction.ASC, "eventDate"));

        } else {
            page = PageRequest.of(from > 0 ? from / size : 0, size,
                    Sort.by(Sort.Direction.DESC, "views"));
        }

        statsClient.hit("ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now());

        Page<Event> events = eventStorage.findAll(conditions, page);
        return events.map(eventMapper::toDto).getContent();
    }

    @Override
    public EventFullDto userGetById(Long eventId, HttpServletRequest request) {
        Event event = eventStorage.findPublishedEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Event: Событие с id=" + eventId + " не найдено"));
        List<ViewStats> stats = statsClient.stats("1900-01-01 00:00:00",
                "2100-01-01 00:00:00",
                request.getRequestURI(),
                request.getRemoteAddr());
        log.info("Saved stats: {}", stats);
        statsClient.hit("ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now());
        if (stats.isEmpty()) {
            event.setViews(event.getViews() + 1);
            return eventMapper.toDto(eventStorage.save(event));
        }
        return eventMapper.toDto(event);
    }

    public static BooleanExpression makeAdminEventQueryFilters(GetAdminEvent getAdminEvent) {
        List<BooleanExpression> conditions = new ArrayList<>();

        if (getAdminEvent.getCategories() != null) {
            conditions.add(qEvent.category.in(getAdminEvent.getCategories()));
        }

        if (getAdminEvent.getStates() != null) {
            conditions.add(qEvent.state.in(getAdminEvent.getStates()));
        }

        if (getAdminEvent.getUsers() != null) {
            conditions.add(qEvent.initiator.id.in(getAdminEvent.getUsers()));
        }
        LocalDateTime rangeStart = getAdminEvent.getRangeStart() != null ? getAdminEvent.getRangeStart() : LocalDateTime.now();
        conditions.add(qEvent.eventDate.goe(rangeStart));

        if (getAdminEvent.getRangeEnd() != null) {
            conditions.add(
                    qEvent.eventDate.loe(getAdminEvent.getRangeEnd())
            );
        }
        return conditions
                .stream()
                .reduce(BooleanExpression::and)
                .get();
    }

    public static BooleanExpression makeUserEventQueryFilters(GetUserEvent getUserEvent) {
        List<BooleanExpression> conditions = new ArrayList<>();

        if (getUserEvent.getText() != null) {
            String textToSearch = getUserEvent.getText();;
            conditions.add(qEvent.title.containsIgnoreCase(textToSearch)
                    .or(qEvent.annotation.containsIgnoreCase(textToSearch))
                    .or(qEvent.description.containsIgnoreCase(textToSearch)));
        }

        if (getUserEvent.getCategories() != null) {
            conditions.add(qEvent.category.in(getUserEvent.getCategories()));
        }

        if (getUserEvent.getPaid() != null) {
            conditions.add(qEvent.paid.eq(getUserEvent.getPaid()));
        }

        LocalDateTime rangeStart = getUserEvent.getRangeStart() != null ? getUserEvent.getRangeStart() : LocalDateTime.now();
        conditions.add(qEvent.eventDate.goe(rangeStart));

        if (getUserEvent.getRangeEnd() != null) {
            conditions.add(
                    qEvent.eventDate.loe(getUserEvent.getRangeEnd())
            );
        }

        if (getUserEvent.getOnlyAvailable()) {
            conditions.add(qEvent.confirmedRequests.lt(qEvent.participantLimit));
        }

        conditions.add(qEvent.state.eq(EventState.PUBLISHED));

        return conditions
                .stream()
                .reduce(BooleanExpression::and)
                .get();
    }
}
