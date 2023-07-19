package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.request.dto.RequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventFullDto> adminFindEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                       String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto adminUpdateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> privateGetCreatorsEvents(Long userId, Pageable pageable);

    EventFullDto privateCreateEvent(Long userId, NewEventDto newEventDto);

    EventFullDto privateGetEventInfoByCreator(Long userId, Long eventId);

    EventFullDto privateUpdateEventByCreator(Long userID, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> publicFindEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, Pageable pageable,
                                         HttpServletRequest request);

    EventFullDto publicFindEventById(Long id, HttpServletRequest request);

    List<RequestDto> getEventRequests(long userId, long eventId);
}
