package ru.practicum.ewm.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.UpdateCategoryDto;
import ru.practicum.ewm.category.model.Category;

@UtilityClass
public class CategoryMapper {
    public Category toCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getId(), categoryDto.getName());
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public CategoryDto toCategoryDto(UpdateCategoryDto updateCategoryDto) {
        return new CategoryDto(0L, updateCategoryDto.getName());
    }
}
