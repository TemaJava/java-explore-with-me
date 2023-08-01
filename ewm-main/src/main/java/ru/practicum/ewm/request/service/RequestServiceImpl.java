package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestsRepository;

    @Transactional
    @Override
    public RequestDto createNewRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("User not found");
        });
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });

        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ValidationException("Event is not published, you cant send request");
        }
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("You cant send request to your event");
        }
        int confirmedRequests = requestsRepository.findByEventIdConfirmed(eventId).size();
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ValidationException("Over participants limit");
        }
        RequestStatus status = RequestStatus.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }
        Request request = new Request(null,
                LocalDateTime.now(),
                event,
                user,
                status);
        Optional<Request> check = requestsRepository
                .findByEventIdAndRequesterId(eventId, userId);
        if (check.isPresent()) throw new ValidationException("Request already created");
        request = requestsRepository.save(request);
        return RequestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestsRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() -> {
                    throw new ObjectNotFoundException("Request not found");
                }
        );
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestsRepository.save(request));
    }

    @Override
    public List<RequestDto> findEventsRequests(Long eventId, Long userId) {
        return requestsRepository
                .findAllByEventIdAndEventInitiatorId(eventId, userId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> findUsersRequests(Long userId) {
        return requestsRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Confirmation error");
        }
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ObjectNotFoundException("You havent event with this id");
        }

        EventRequestStatusUpdateResult requestUpdateDto =
                new EventRequestStatusUpdateResult(new ArrayList<>(), new ArrayList<>());
        Integer confirmedRequests = requestsRepository.findByEventIdConfirmed(eventId).size();
        List<Request> requests = requestsRepository.findByEventIdAndRequestsIds(eventId,
                eventRequestStatusUpdateRequest.getRequestIds());
        if (Objects.equals(eventRequestStatusUpdateRequest.getStatus(), RequestStatus.CONFIRMED.name())
                && confirmedRequests + requests.size() > event.getParticipantLimit()) {
            requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            List<RequestDto> requestDto = requests.stream()
                    .map(RequestMapper::toRequestDto)
                    .collect(Collectors.toList());
            requestUpdateDto.setRejectedRequests(requestDto);
            requestsRepository.saveAll(requests);
            throw new ValidationException("Requests limit exceeded");
        }
        if (eventRequestStatusUpdateRequest.getStatus().equalsIgnoreCase(RequestStatus.REJECTED.name())) {
            requests.forEach(request -> {
                if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                    throw new ValidationException("You can't reject confirmed request");
                }
                request.setStatus(RequestStatus.REJECTED);
            });
            List<RequestDto> requestDto = requests.stream()
                    .map(RequestMapper::toRequestDto)
                    .collect(Collectors.toList());
            requestUpdateDto.setRejectedRequests(requestDto);
            requestsRepository.saveAll(requests);
        } else if (eventRequestStatusUpdateRequest.getStatus().equalsIgnoreCase(RequestStatus.CONFIRMED.name())
                && eventRequestStatusUpdateRequest.getRequestIds().size() <= event.getParticipantLimit() - confirmedRequests) {
            requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
            List<RequestDto> requestDto = requests.stream()
                    .map(RequestMapper::toRequestDto)
                    .collect(Collectors.toList());
            requestUpdateDto.setConfirmedRequests(requestDto);
            requestsRepository.saveAll(requests);
        }
        return requestUpdateDto;
    }
}
