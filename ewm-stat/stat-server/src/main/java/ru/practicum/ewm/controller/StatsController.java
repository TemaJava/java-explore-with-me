package ru.practicum.ewm.controller;


import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.StatsService;
import ru.practicum.ewm.dto.EndpointHitDto;
import org.springframework.http.HttpStatus;
import ru.practicum.ewm.dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto create(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        return statsService.create(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam String start, @RequestParam String end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        return statsService.getStats(LocalDateTime.parse(start, FORMATTER),
                LocalDateTime.parse(end, FORMATTER), uris, unique);
    }
}
