package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.pagination.Pagination;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventShortDto> privateGetCreatorsEvents(@PathVariable Long userId,
                                                        @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                        @Positive @RequestParam(defaultValue = "10") int size) {
        return eventService.privateGetCreatorsEvents(userId, Pagination.toPageable(from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto privateCreateEvent(@PathVariable Long userId,
                                           @RequestBody @Validated  NewEventDto newEventDto) {
        return eventService.privateCreateEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto privateGetEventInfoByCreator(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        return eventService.privateGetEventInfoByCreator(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto privateUpdateEventByCreator(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody @Validated UpdateEventRequest updateEventRequest) {
        return eventService.privateUpdateEventByCreator(userId, eventId, updateEventRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> privateFindEventsRequests(@PathVariable Long userId,
                                                      @PathVariable Long eventId) {
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@Positive @PathVariable Long userId,
                                                              @Positive @PathVariable Long eventId,
                                                              @Validated @RequestBody EventRequestStatusUpdateRequest
                                                                          eventRequestStatusUpdateRequest) {
        return requestService.updateRequestStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }
}