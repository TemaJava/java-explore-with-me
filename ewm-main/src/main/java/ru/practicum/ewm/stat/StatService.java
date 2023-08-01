package ru.practicum.ewm.stat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.dto.EndpointHitDto;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatService {
    private final StatsClient statsClient;

    public ResponseEntity<Object> getViewStats(
            String rangeStart,
            String rangeEnd,
            List<String> uris,
            Boolean unique) {
        return statsClient.getViewStats(rangeStart, rangeEnd, uris, unique);
    }

    @Transactional
    public void createView(EndpointHitDto endpointHitDto) {
        statsClient.create(endpointHitDto);
    }
}
