package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository repository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Pageable pageable) {
        return repository.findAllByPinned(pinned, pageable).stream()
                .map(CompilationMapper::toCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = repository.findById(compId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Compilation not found");
        });
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = eventRepository.findByIds(newCompilationDto.getEvents());
        if (newCompilationDto.getPinned() == null) newCompilationDto.setPinned(false);
        return CompilationMapper.toCompilationDto(repository.save(CompilationMapper
                .toCompilation(newCompilationDto, events)));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long id, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = repository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Compilation not found");
        });
        if (updateCompilationDto.getTitle() != null) {
            if (updateCompilationDto.getTitle().length() > 50) {
                throw new BadRequestException("Title length more then 50");
            }
        }
        if (updateCompilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(updateCompilationDto.getEvents()));
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        compilation.setId(id);
        repository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long id) {
        repository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Compilation to delete not found");
        });
        repository.deleteById(id);
    }
}
