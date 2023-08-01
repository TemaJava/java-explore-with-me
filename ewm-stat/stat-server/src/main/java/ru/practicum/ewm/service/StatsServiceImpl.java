package ru.practicum.ewm.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.mapper.StatsMapper;
import ru.practicum.ewm.model.ViewStats;
import ru.practicum.ewm.repository.StatsRepository;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.mapper.StatsMapper.toEndpointHit;
import static ru.practicum.ewm.mapper.StatsMapper.toEndpointHitDto;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHitDto create(EndpointHitDto endpointHitDto) {
        return toEndpointHitDto(statsRepository.save(toEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start cant be after end");
        }
        List<ViewStats> hits;
        if (unique && uris != null) {
            hits = statsRepository.getUniqueStatsByUrisAndTimestamps(start, end, uris);
        } else if (!unique && uris != null) {
            hits = statsRepository.getStatsByUrisAndTimestamps(start, end, uris);
        } else if (!unique) {
            hits = statsRepository.getAllStats(start, end);
        } else {
            hits = statsRepository.getAllUniqueStats(start, end);
        }

        return hits.stream().map(StatsMapper::toViewStatsDto).collect(toList());
    }
}