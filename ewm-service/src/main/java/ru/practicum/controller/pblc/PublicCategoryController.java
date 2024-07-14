package ru.practicum.controller.pblc;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;
import ru.practicum.util.ApiPathConstants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.CATEGORY_PATH)
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> get(@RequestParam(defaultValue = "0") int from,
                                 @RequestParam(defaultValue = "10") int size) {
        return categoryService.get(from, size);
    }

    @GetMapping(ApiPathConstants.BY_ID_PATH)
    public CategoryDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }
}
