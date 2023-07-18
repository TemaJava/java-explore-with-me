package ru.practicum.ewm.stat;

import ru.practicum.ewm.dto.EndpointHitDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

public class HitMapper {
    public static EndpointHitDto toEndpointHit(String app, HttpServletRequest request) {
        return new EndpointHitDto(null,
                app,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now());
    }
}
