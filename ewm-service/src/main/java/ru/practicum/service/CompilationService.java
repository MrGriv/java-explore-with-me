package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    ResponseEntity<CompilationDto> add(NewCompilationDto newCompilationDto);

    ResponseEntity<Void> delete(Long compilationId);

    CompilationDto update(UpdateCompilationRequest updateCompilation, Long compilationId);

    List<CompilationDto> get(Boolean pinned, int from, int size);

    CompilationDto getById(Long compilationId);
}
