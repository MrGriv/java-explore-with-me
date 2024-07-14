package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(defaultValue = "false", target = "pinned")
    Compilation toEntity(NewCompilationDto newCompilationDto);

    @Mapping(target = "events", expression = "java(setIShortEventDtoId(compilation.getEvents()))")
    CompilationDto toDto(Compilation compilation);

    default List<EventShortDto> setIShortEventDtoId(List<Long> events) {
        if (events == null) {
            return new ArrayList<>();
        }
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (Long id : events) {
            EventShortDto event = new EventShortDto();
            event.setId(id);
            eventShortDtos.add(event);
        }
        return eventShortDtos;
    }
}
