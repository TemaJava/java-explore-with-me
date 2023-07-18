package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {
    private final RequestService service;

    @GetMapping
    public List<RequestDto> findUsersRequests(@Positive @PathVariable Long userId) {
        return service.findUsersRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createNewRequest(@Positive @PathVariable Long userId,
                                       @RequestParam Long eventId) {
        return service.createNewRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelRequest(@Positive @PathVariable Long userId,
                                    @Positive @PathVariable Long requestId) {
        return service.cancelRequest(userId, requestId);
    }
}
