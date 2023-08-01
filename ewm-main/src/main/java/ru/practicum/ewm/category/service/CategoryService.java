package ru.practicum.ewm.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto findCategoryById(long id);

    List<CategoryDto> findAllCategory(Pageable pageable);

    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(UpdateCategoryDto categoryDto, long id);
}
