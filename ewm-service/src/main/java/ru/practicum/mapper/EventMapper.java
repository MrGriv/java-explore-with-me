package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.event.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(defaultValue = "false", target = "paid")
    @Mapping(defaultValue = "0", target = "participantLimit")
    @Mapping(defaultValue = "true", target = "requestModeration")
    Event toEntity(NewEventDto newEventDto);

    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventFullDto toDto(Event event);

    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto toShortDto(Event event);
}
