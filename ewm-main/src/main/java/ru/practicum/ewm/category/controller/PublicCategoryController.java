package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.user.pagination.Pagination;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoryController {
    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> findAllCategories(@PositiveOrZero @RequestParam (defaultValue = "0") Integer from,
                                               @Positive @RequestParam (defaultValue = "10") Integer size) {
        return service.findAllCategory(Pagination.toPageable(from, size));
    }

    @GetMapping("/{catId}")
    public CategoryDto findCategoryById(@Positive @PathVariable Integer catId) {
        return service.findCategoryById(catId);
    }

}
