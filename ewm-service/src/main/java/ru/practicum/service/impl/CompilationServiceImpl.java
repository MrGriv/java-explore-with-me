package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.service.CompilationService;
import ru.practicum.storage.CompilationStorage;
import ru.practicum.storage.EventStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationStorage compilationStorage;
    private final CompilationMapper compilationMapper;
    private final EventStorage eventStorage;
    private final EventMapper eventMapper;

    @Override
    public ResponseEntity<CompilationDto> add(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationStorage.save(compilationMapper.toEntity(newCompilationDto));
        List<Event> events = compilation.getEvents() == null ? new ArrayList<>() :
                eventStorage.findAllByIdIn(compilation.getEvents());
        CompilationDto compilationDto = compilationMapper.toDto(compilation);
        compilationDto.setEvents(events.stream().map(eventMapper::toShortDto).collect(Collectors.toList()));
        return new ResponseEntity<>(compilationDto, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> delete(Long compilationId) {
        compilationStorage.deleteById(compilationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<CompilationDto> update(UpdateCompilationRequest updateCompilation,
                                 Long compilationId) {
        Compilation compilation = compilationStorage.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation: Список событий с id=" + compilationId +
                        " не найден"));
        compilation.setEvents(updateCompilation.getEvents() == null ? compilation.getEvents() :
                updateCompilation.getEvents());
        compilation.setPinned(updateCompilation.getPinned() != null ? updateCompilation.getPinned() :
                compilation.getPinned());
        compilation.setTitle(updateCompilation.getTitle() == null ? compilation.getTitle() :
                updateCompilation.getTitle());
        Compilation savedCompilation = compilationStorage.save(compilation);
        List<Event> events = eventStorage.findAllByIdIn(savedCompilation.getEvents());
        CompilationDto compilationDto = compilationMapper.toDto(savedCompilation);
        compilationDto.setEvents(events.stream().map(eventMapper::toShortDto).collect(Collectors.toList()));
        return new ResponseEntity<>(compilationDto, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> get(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationStorage.findAllByPinned(pinned, page);
        } else {
            compilations = compilationStorage.findAll(page);
        }
        if (compilations == null) {
            return new ArrayList<>();
        }
        List<Long> eventIds = new ArrayList<>();
        for (Compilation compilation : compilations) {
            eventIds.addAll(compilation.getEvents());
        }
        List<CompilationDto> compilationDtos = compilations.map(compilationMapper::toDto).getContent();
        Map<Long, Event> events = eventStorage.findAllByIdIn(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        for (CompilationDto compilation : compilationDtos) {
            List<EventShortDto> eventShortDtos = new ArrayList<>();
            for (EventShortDto event : compilation.getEvents()) {
                eventShortDtos.add(eventMapper.toShortDto(events.get(event.getId())));
            }
            compilation.setEvents(eventShortDtos);
        }
        return compilationDtos;
    }

    @Override
    public CompilationDto getById(Long compilationId) {
        Compilation compilation = compilationStorage.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation: Список событий с id=" + compilationId +
                        " не найден"));
        List<Event> events = eventStorage.findAllByIdIn(compilation.getEvents());
        CompilationDto compilationDto = compilationMapper.toDto(compilation);
        compilationDto.setEvents(events.stream().map(eventMapper::toShortDto).collect(Collectors.toList()));
        return compilationDto;
    }
}
