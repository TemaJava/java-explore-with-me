package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.UpdateCategoryDto;
import ru.practicum.ewm.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.exception.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;

    @Override
    public CategoryDto findCategoryById(long id) {
        Category category = repository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Category not found");
        });
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> findAllCategory(Pageable pageable) {
        return repository.findAll(pageable).stream().map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        return CategoryMapper.toCategoryDto(repository.save(CategoryMapper.toCategory(categoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        repository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Category not found");
        });
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(UpdateCategoryDto categoryDto, long id) {
        Category category = repository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Category not found");
        });
        if (categoryDto.getName().equals(category.getName())) {
            return CategoryMapper.toCategoryDto(category);
        }

        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(repository.save(category));
    }
}
