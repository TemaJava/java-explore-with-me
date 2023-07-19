package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;


import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService service;

    @GetMapping
    public List<EventFullDto> findAllEvents(@PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                                            @Positive @RequestParam(required = false, defaultValue = "10") int size,
                                            @RequestParam(name = "users", required = false) List<Long> usersId,
                                            @RequestParam(required = false) List<EventState> states,
                                            @RequestParam(name = "categories", required = false) List<Long> categoriesId,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd) {
        return service.adminFindEvents(usersId, states, categoriesId, rangeStart,
                rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody @Validated UpdateEventAdminRequest updateEventAdminRequest) {
        return service.adminUpdateEvent(eventId, updateEventAdminRequest);
    }
}
