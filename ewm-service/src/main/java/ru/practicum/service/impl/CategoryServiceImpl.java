package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.service.CategoryService;
import ru.practicum.storage.CategoryStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryStorage categoryStorage;
    private final CategoryMapper categoryMapper;

    @Override
    public ResponseEntity<CategoryDto> add(NewCategoryDto newCategoryDto) {
        Category cat = categoryMapper.toEntity(newCategoryDto);
        Category category = categoryStorage.save(cat);
        return new ResponseEntity<>(categoryMapper.toDto(category), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        categoryStorage.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public CategoryDto update(Long id, NewCategoryDto newCategoryDto) {
        Category updatedCategory = categoryStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Category: Категория с id=" + id + " не найдена"));
        if (!updatedCategory.getName().equals(newCategoryDto.getName())) {
            updatedCategory.setName(newCategoryDto.getName());
            return categoryMapper.toDto(categoryStorage.save(updatedCategory));
        }
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> get(int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Category> categories = categoryStorage.findAll(page);
        return categories.isEmpty() ? new ArrayList<>() : categories.map(categoryMapper::toDto).getContent();
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        Category category = categoryStorage.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category: Категория с id=" + categoryId + " не найдена"));
        return categoryMapper.toDto(category);
    }
}
