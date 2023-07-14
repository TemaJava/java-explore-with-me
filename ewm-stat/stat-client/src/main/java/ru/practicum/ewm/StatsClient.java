package ru.practicum.ewm;

import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.ewm.dto.EndpointHitDto;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ViewStatsDto;


@Service
public class StatsClient {
    private final WebClient webClient;

    public StatsClient(@Value("${ewm-stats-server.url}") String serverUrl) {
        webClient = WebClient.builder()
                .baseUrl(serverUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void saveEndpointHit(EndpointHitDto createEndpointHitDto) {
        webClient.post()
                .uri("/hit")
                .body(Mono.just(createEndpointHitDto), EndpointHitDto.class)
                .retrieve();
    }

    public Mono<List<ViewStatsDto>> getStatistics() {
        return webClient.get()
                .uri("/stats")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ViewStatsDto>>() {});
    }
}