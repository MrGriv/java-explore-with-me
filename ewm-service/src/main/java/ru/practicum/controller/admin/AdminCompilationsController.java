package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;
import ru.practicum.util.ApiPathConstants;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ADMIN_COMPILATIONS_PATH)
public class AdminCompilationsController {
    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> add(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        return compilationService.add(newCompilationDto);
    }

    @DeleteMapping(ApiPathConstants.BY_ID_PATH)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return compilationService.delete(id);
    }

    @PatchMapping(ApiPathConstants.BY_ID_PATH)
    public CompilationDto update(@Valid @RequestBody UpdateCompilationRequest updateCompilation,
                                 @PathVariable Long id) {
        return compilationService.update(updateCompilation, id);
    }
}
