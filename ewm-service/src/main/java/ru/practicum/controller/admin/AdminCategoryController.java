package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.CategoryService;
import ru.practicum.util.ApiPathConstants;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ADMIN_CATEGORY_PATH)
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> add(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.add(newCategoryDto);
    }

    @DeleteMapping(ApiPathConstants.BY_ID_PATH)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }

    @PatchMapping(ApiPathConstants.BY_ID_PATH)
    public CategoryDto update(@PathVariable Long id,
                              @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.update(id, newCategoryDto);
    }
}
