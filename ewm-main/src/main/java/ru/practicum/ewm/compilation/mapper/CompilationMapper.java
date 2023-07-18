package ru.practicum.ewm.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
    public Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
        return new Compilation(null,
                events,
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle());
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> shortEvents = compilation.getEvents().stream()
                .map(EventMapper::toShortEventDto).collect(Collectors.toList());
        return new CompilationDto(compilation.getId(),
                shortEvents,
                compilation.getPinned(),
                compilation.getTitle());
    }
}
