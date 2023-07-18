package ru.practicum.ewm.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.pagination.Pagination;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/compilations")
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam (required = false) boolean pinned,
                                                   @PositiveOrZero @RequestParam (defaultValue = "0") Integer from,
                                                   @Positive @RequestParam (defaultValue = "10") Integer size) {
        return service.getAllCompilations(pinned, Pagination.toPageable(from, size));
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        return service.getById(compId);
    }
}
