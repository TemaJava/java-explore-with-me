package ru.practicum.ewm.controller;


import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.StatsService;
import ru.practicum.ewm.dto.EndpointHitDto;
import org.springframework.http.HttpStatus;
import ru.practicum.ewm.dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto saveEndpointHit(@Valid @RequestBody EndpointHitDto createEndpointHitDto) {
        return statsService.create(createEndpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStats(@RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        return statsService.getStats(start, end, uris, unique);
    }
}
