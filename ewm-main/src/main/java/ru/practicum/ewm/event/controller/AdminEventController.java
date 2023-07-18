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
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService service;

    @GetMapping
    public List<EventFullDto> findAllEvents(@RequestParam(name = "users", required = false) List<Long> usersId,
                                            @RequestParam(required = false) List<EventState> states,
                                            @RequestParam(name = "categories", required = false) List<Long> categoriesId,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                            @Positive @RequestParam(defaultValue = "10") int size) {
        return service.adminFindEvents(usersId, states, categoriesId, rangeStart,
                rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody @Validated UpdateEventAdminRequest updateEventAdminRequest) {
        return service.adminUpdateEvent(eventId, updateEventAdminRequest);
    }
}
