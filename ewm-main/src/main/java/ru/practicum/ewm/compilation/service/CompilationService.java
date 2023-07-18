package ru.practicum.ewm.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getAllCompilations(Boolean pinned, Pageable pageable);

    CompilationDto getById(Long compId);

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long id, UpdateCompilationDto newCompilationDto);

    void deleteCompilation(Long id);
}
