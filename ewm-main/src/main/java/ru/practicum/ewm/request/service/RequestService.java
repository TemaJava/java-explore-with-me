package ru.practicum.ewm.request.service;

import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    List<RequestDto> findUsersRequests(Long userId);

    RequestDto createNewRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> findEventsRequests(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
