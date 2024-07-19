package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    ResponseEntity<CategoryDto> add(NewCategoryDto newCategoryDto);

    ResponseEntity<Void> delete(Long categoryId);

    ResponseEntity<CategoryDto> update(Long id, NewCategoryDto newCategoryDto);

    List<CategoryDto> get(int from, int size);

    CategoryDto getById(Long categoryId);
}
