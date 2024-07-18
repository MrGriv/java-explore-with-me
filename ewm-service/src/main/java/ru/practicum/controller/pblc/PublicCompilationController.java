package ru.practicum.controller.pblc;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.CompilationService;
import ru.practicum.util.ApiPathConstants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.COMPILATIONS_PATH)
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> get(@RequestParam(required = false) Boolean pinned,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return compilationService.get(pinned, from, size);
    }

    @GetMapping(ApiPathConstants.BY_ID_PATH)
    public CompilationDto getById(@PathVariable Long id) {
        return compilationService.getById(id);
    }
}
